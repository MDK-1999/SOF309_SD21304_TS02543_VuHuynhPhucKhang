# TÀI LIỆU ĐẶC TẢ NGHIỆP VỤ HỆ THỐNG (SPECIFICATIONS)

> **Dự án:** GearAstral – Web bán linh kiện máy tính<br>
> **Sinh viên:** Vũ Huỳnh Phúc Khang · TS02543 · SD21304<br>
> **Môn học:** SOF309

---

## 1. THÔNG TIN CHUNG

* **Tên ứng dụng:** GearAstral.
* **Công nghệ:** Java 17, Servlet/JSP, JSTL, JDBC, Maven và Jetty 12. Bộ kiểm thử dùng JUnit 5 và Selenium WebDriver 4.
* **Cổng chạy local:** `http://localhost:8080`. Trang gốc chuyển đến danh mục `/shop`.
* **Cơ sở dữ liệu:** H2 trong bộ nhớ cho demo. Cấu hình triển khai dùng SQL Server qua JDBC.

---

## 2. QUY TẮC NGHIỆP VỤ CHI TIẾT (BUSINESS RULES)

### A. Chức năng Xác thực & Quản lý Tài khoản (Auth)

* **Đăng nhập (`/login`):**
  * `username` bắt buộc và phải hợp lệ về định dạng. `password` bắt buộc, không được toàn khoảng trắng, tối đa 72 byte UTF-8.
  * Tài khoản hoạt động đăng nhập đúng sẽ đổi mã phiên và chuyển đến `/dashboard`. Dashboard chuyển khách hàng đến `/customer/orders`. Tài khoản nhân viên demo là `demostaff`, dùng mật khẩu cấu hình trong [hướng dẫn chạy](../README.md).
  * Sai dữ liệu, sai mật khẩu hoặc tài khoản ngừng hoạt động đều chuyển về `/login`. Thông báo phân biệt dữ liệu sai định dạng với đăng nhập thất bại.
  * Sau 5 lần sai thông tin đăng nhập hợp lệ về định dạng từ cùng IP và tên đăng nhập, lần tiếp theo bị tạm khóa trong 5 phút kể từ lúc đạt ngưỡng. Phản hồi có header `Retry-After`.
* **Đăng ký tài khoản mới (`/register`):**
  * `username` dài 4–32 ký tự ASCII gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang. Tên phải duy nhất, không phân biệt hoa thường.
  * `password` tối thiểu 8 ký tự theo Java, tối đa 72 byte UTF-8, có chữ hoa, chữ thường và chữ số. `confirm` phải khớp chính xác. Hệ thống băm mật khẩu bằng bcrypt khi lưu.
  * `fullName` dài 2–100 ký tự sau chuẩn hóa. Giá trị dài hơn 100 ký tự gửi qua HTTP được cắt còn 100 ký tự.
  * `email` tùy chọn, tối đa 100 ký tự sau chuẩn hóa. Nếu có, email phải có phần tên, dấu `@` và hậu tố tên miền gồm 2–63 chữ cái. Email phải duy nhất, không phân biệt hoa thường.
  * `phone` bắt buộc. Sau khi bỏ dấu cách, chấm, ngoặc và gạch ngang, số phải có tiền tố `0` hoặc `+84` và 9–10 chữ số tiếp theo.
  * `address` tùy chọn, cắt còn tối đa 200 ký tự sau chuẩn hóa.
  * Trường văn bản được bỏ khoảng trắng hai đầu và gộp khoảng trắng liên tiếp. Mật khẩu được so sánh nguyên văn.
  * Thành công tạo tài khoản `customer` và chuyển đến `/login?success=registered`. Dữ liệu sai hoặc trùng được phát hiện trước khi lưu trả HTTP 400. DAO lưu thất bại trả HTTP 409.
* **Phân quyền và phiên:**
  * Khách có thể xem `/shop` và chi tiết `/product?id=...`. Chỉ `staff` và `admin` được truy cập `/staff/products`.
  * Khách truy cập khu vực nhân viên được chuyển đến `/login?error=session`. Khách hàng đã đăng nhập được chuyển đến `/dashboard?error=forbidden`.
  * POST yêu cầu token CSRF hợp lệ của phiên qua trường `_csrf` hoặc header `X-CSRF-Token`. Thiếu hoặc sai token trả HTTP 403.
  * `POST /logout` kết thúc phiên và chuyển về `/login`.

### B. Chức năng Đặt hàng & Thanh toán (Payment)

* **Đặt hàng trên web (`POST /checkout`):**
  * Giỏ trống chuyển đến `/cart?error=empty`. Tên người nhận cần ít nhất 2 ký tự, điện thoại phải hợp lệ và địa chỉ cần ít nhất 5 ký tự. Email tùy chọn, cần đúng định dạng nếu có. Dữ liệu sai trả HTTP 400.
  * Hệ thống tính tổng từ giá hiện tại nhân với số lượng của từng sản phẩm. Hàng ngừng bán hoặc thiếu tồn kho trả HTTP 409.
  * Đơn web dùng phương thức `cod` (thanh toán khi nhận hàng). Khi tạo thành công, đơn ở trạng thái `pending`, trang hiển thị mã đơn và giỏ hàng được làm trống.
* **Cập nhật trạng thái thanh toán (`POST /staff/orders`, `action=payment`):**
  * Nhân viên gửi `orderId` dương và `paymentStatus` thuộc `unpaid`, `paid` hoặc `refunded`.
  * Cập nhật thành công chuyển đến `/staff/orders?success=updated`. Trạng thái không hợp lệ hoặc cập nhật thất bại chuyển đến `/staff/orders?error=payment`.
* Các ca của mục B được thiết kế cho đợt kiểm thử mở rộng, sau phần nghiệm thu xác thực và sản phẩm của Tuần 1.

### C. Module CRUD Quản lý Sản phẩm (Product)

* **Thêm mới (Create):** Gửi `POST /staff/products` với `action=add`. Tạo sản phẩm đang bán và chuyển đến `/staff/products?success=add` khi thành công.
  * `catId` là ID dương của danh mục tồn tại. `productName` dài 2–200 ký tự sau chuẩn hóa và cắt độ dài.
  * `price` từ 0 đến 1.000.000.000 đồng, bao gồm hai đầu.
  * `stockQty` bắt buộc khi thêm, là số nguyên từ 0 đến 100.000.
  * `brand`, `model`, `description` và `specs` là tùy chọn. Controller cắt còn lần lượt 80, 100, 2.000 và 1.000 ký tự.
* **Xem danh sách (Read):** `GET /staff/products` liệt kê cả sản phẩm đang bán và ngừng bán. Tham số `q` tìm theo tên hoặc hãng. `GET /product?id=...` trả HTTP 200 cho sản phẩm đang bán và 404 nếu ID thiếu, sai, không tồn tại hoặc sản phẩm ngừng bán.
* **Cập nhật (Update):** Gửi `POST /staff/products` với `action=update` và `productId` của sản phẩm tồn tại. Giữ nguyên tồn kho khi sửa. Thành công chuyển đến `/staff/products?success=update`.
* **Ngừng bán và mở bán lại (Delete/Restore):** Sửa sản phẩm, đặt `isActive=false` để ẩn khỏi danh mục và chi tiết công khai. Bản ghi vẫn có trong danh sách nhân viên. Đặt `isActive=true` để hiển thị lại.
* Thao tác sai dữ liệu, sai `action` hoặc cập nhật ID không tồn tại chuyển đến `/staff/products?error=invalid`.
