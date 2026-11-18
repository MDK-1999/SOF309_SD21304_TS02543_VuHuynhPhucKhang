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

## 2. NGƯỜI DÙNG VÀ PHÂN QUYỀN

| Đối tượng | Phạm vi | Sau đăng nhập |
| --- | --- | --- |
| Khách | Xem cửa hàng, chi tiết, giỏ hàng, checkout, PC Builder và form tài khoản. | Chưa có phiên đăng nhập. |
| customer | Chức năng công khai và danh sách, chi tiết đơn thuộc tài khoản mình. | `/dashboard` chuyển tới `/customer/orders`. |
| staff | Dashboard, sản phẩm, đơn hàng, nhập kho và hóa đơn. | Dashboard nhân viên. |
| admin | Quyền staff, quản lý danh mục, tài khoản và báo cáo. | Dashboard quản trị. |

`AuthFilter` kiểm tra vai trò trên `/admin/*`, `/staff/*` và `/customer/*`. Khách vào vùng bảo vệ được chuyển tới `/login?error=session`. Người có phiên sai quyền được chuyển tới `/dashboard?error=forbidden`. Vai trò admin được phép vào vùng staff theo `User.isStaff()`.

## 3. URL MÀN HÌNH VÀ ĐIỂM XỬ LÝ

URL trong bảng là đường dẫn tương đối với context của ứng dụng. Jetty chạy tại `http://localhost:8080`. Nếu deploy WAR trên Tomcat cần thêm context, ví dụ `/GearAstral-SOF309`.

| URL | Phương thức | Chức năng | Quyền |
| --- | --- | --- | --- |
| `/`, `/index.jsp` | GET | Chuyển tới cửa hàng | Công khai |
| `/shop` | GET | Tìm, lọc, sắp xếp và phân trang linh kiện | Công khai |
| `/product?id=...` | GET | Chi tiết sản phẩm đang bán | Công khai |
| `/product-image?id=...` | GET | Ảnh sản phẩm | Công khai |
| `/login` | GET, POST | Form và xử lý đăng nhập | Công khai |
| `/register` | GET, POST | Đăng ký tài khoản customer | Công khai |
| `/cart` | GET, POST | Xem, thêm, sửa số lượng, xóa dòng giỏ | Công khai |
| `/checkout` | GET, POST | Thông tin giao hàng và tạo đơn COD | Công khai |
| `/pc-builder` | GET, POST | Chọn cấu hình và đưa linh kiện vào giỏ | Công khai |
| `/ai-chat` | POST | Tư vấn cấu hình, phụ thuộc cấu hình AI | Công khai, có CSRF và giới hạn lượt |
| `/dashboard` | GET | Điều hướng khách hàng hoặc hiện dashboard | Có phiên |
| `/logout` | POST | Kết thúc phiên | Có phiên |
| `/customer/orders` | GET | Danh sách đơn và chi tiết qua `id` | customer |
| `/staff/products` | GET, POST | Thêm, xem, sửa, ngừng bán và mở bán lại | staff, admin |
| `/staff/stock` | GET, POST | Xem và nhập kho | staff, admin |
| `/staff/orders` | GET, POST | Tạo đơn, cập nhật trạng thái đơn hoặc thanh toán | staff, admin |
| `/staff/invoice` | GET | In hóa đơn | staff, admin |
| `/admin/categories` | GET, POST | Quản lý danh mục | admin |
| `/admin/users` | GET, POST | Quản lý tài khoản | admin |
| `/admin/report` | GET | Báo cáo | admin |
| `/robots.txt`, `/sitemap.xml` | GET | Thông tin lập chỉ mục | Công khai |

Các route được đối chiếu với annotation `@WebServlet`, bộ lọc và JSP trong `src/main` ngày 18/11/2026.

## 4. QUY TẮC NGHIỆP VỤ VÀ VALIDATION

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

### D. Cửa hàng, giỏ hàng và đơn của khách

* `/shop` nhận `q` tối đa 100 ký tự, `brand` tối đa 80 ký tự, `catId` dương, `min`/`max` từ 0 đến 1.000.000.000. Nếu min lớn hơn max, controller hoán đổi hai giá trị. Mỗi trang có tối đa 24 sản phẩm. Trang yêu cầu lớn hơn tổng số trang được đưa về trang cuối.
* Giỏ được lưu theo session. `/cart` nhận `action`, `productId` và `qty`. Giá trị qty không phải số nguyên 1–99 được đưa về 1. Lượng thực tế được chặn theo tồn kho và 99. Sản phẩm thiếu, ngừng bán hoặc hết tồn chuyển tới `/cart?error=unavailable`.
* `action=remove` xóa dòng theo productId rồi chuyển về giỏ. Giỏ trống hiển thị tổng bằng 0.
* Checkout nhận `customerName`, `customerPhone`, `customerEmail`, `shippingAddr`, `notes`. Các trường giao hàng có thể lấy từ thông tin tài khoản khi yêu cầu gửi rỗng.
* Trước khi tạo đơn, controller đọc lại giá và tồn kho hiện tại. `OrderDAO.createWithItems` thực hiện lưu đơn và cập nhật kho theo giao dịch. Kết quả thất bại cần được kiểm tra cả trạng thái DB và giỏ.
* Chi tiết `/customer/orders?id=...` trả 404 cho đơn không tồn tại hoặc đơn không thuộc customer hiện tại.

### E. Nhập kho và quản lý đơn

* Nhập kho nhận `productId` dương và `quantity` từ 1 đến 100.000. `unitCost` được đọc trong khoảng 0–1.000.000.000. Giá trị sai được đưa về 0 theo fallback hiện tại. `supplier` cắt còn 150 ký tự, `note` còn 500 ký tự.
* Trạng thái đơn hợp lệ gồm `pending`, `confirmed`, `processing`, `shipping`, `done`, `cancelled`. Controller kiểm tra tập giá trị, chưa áp dụng sơ đồ giới hạn bước chuyển trạng thái.
* Trạng thái thanh toán hợp lệ gồm `unpaid`, `paid`, `refunded`. Web checkout tạo COD. Đơn tạo trong khu vực nhân viên có thêm giá trị phương thức `qr` theo mã hiện tại.
* Danh mục nhận `catName` dài ít nhất 2 và cắt tối đa 100 ký tự. `description` cắt còn 500 ký tự, `icon` còn 50. Các action được hỗ trợ là `add` và `update`.

## 5. PHIÊN, CSRF VÀ DỮ LIỆU KIỂM THỬ

Mỗi POST lấy `_csrf` từ form cùng phiên hoặc header `X-CSRF-Token`. Thiếu hoặc sai token trả HTTP 403 trước khi Servlet ghi dữ liệu. Đăng nhập thành công đổi session ID. Đăng xuất hủy phiên.

Fixture và cách ghi kết quả được mô tả trong [TEST_PLAN.md](TEST_PLAN.md). Các kiểm thử DAO dùng mock chỉ xác nhận hợp đồng gọi và phản hồi Controller. Kiểm tra khóa ngoại, giao dịch trừ kho và quyền sở hữu cần môi trường H2 hoặc SQL Server riêng.

## 6. TRUY VẾT VÀ TRẠNG THÁI NGHIỆM THU

| Phạm vi | Bằng chứng | Trạng thái |
| --- | --- | --- |
| Đăng ký, đăng nhập | `AuthControllerTest`, 20 lượt tham số và test đơn | Đã chạy đạt |
| Form và xử lý sản phẩm | `ProductControllerTest`, 20 lượt | Đã chạy đạt |
| Controller trên GitHub Actions | Commit `214fed9`, [run Tuần 2](https://github.com/MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang/actions/runs/43726837327) | Workflow thành công |
| HTTP toàn web | Lượt smoke ngày 07/11 được lưu trong Test Cases | Bằng chứng lịch sử, phạm vi theo từng dòng |
| Selenium, dữ liệu DB mở rộng, AI | Các ca thiết kế và nhật ký 03 | Chờ thực thi |

Mỗi test case tham chiếu mục nghiệp vụ, bộ dữ liệu và tầng kiểm tra trong [TEST_CASES.md](TEST_CASES.md). Ngày cập nhật tài liệu 18/11/2026.
