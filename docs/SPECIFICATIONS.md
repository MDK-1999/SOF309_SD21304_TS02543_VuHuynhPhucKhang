# Đặc tả nghiệp vụ GearAstral

**SOF309 · Tuần 1 · Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

GearAstral là web bán linh kiện máy tính viết bằng Java 17, Servlet/JSP, JSTL và JDBC. Bản nghiệm thu chạy tại `http://localhost:8080/` và mở danh mục `/shop`. Dùng H2 trong bộ nhớ cho lượt demo. Khi chạy với cấu hình SQL Server của GearAstral, ứng dụng dùng kết nối JDBC gốc.

## Vai trò và phiên

- Khách có thể mở `/shop`, `/product?id=...`, `/login` và `/register`.
- Đăng ký công khai tạo vai trò `customer`. Vai trò `staff` và `admin` được truy cập `/staff/products`.
- Khách truy cập khu vực nhân viên nhận HTTP 302 đến `/login?error=session`. Khách hàng đã đăng nhập nhận 302 đến `/dashboard?error=forbidden`.
- POST yêu cầu mã CSRF lấy từ trường ẩn `_csrf` của form trong cùng phiên hoặc header `X-CSRF-Token`. Thiếu hoặc sai mã nhận HTTP 403.
- `POST /logout` kết thúc phiên và chuyển về `/login`.

## Đăng ký

`GET /register` hiển thị form. `POST /register` xử lý các trường sau.

| Trường | Ràng buộc và xử lý |
| --- | --- |
| `username` | 4–32 ký tự ASCII gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang. Kiểm tra trùng không phân biệt hoa thường. |
| `password` | Ít nhất 8 ký tự theo Java và tối đa 72 byte UTF-8. Có chữ hoa, chữ thường và chữ số. Mật khẩu được băm bằng bcrypt khi lưu. |
| `confirm` | Khớp chính xác với mật khẩu. |
| `fullName` | Từ 2 đến 100 ký tự sau chuẩn hóa. Giá trị quá dài qua HTTP được cắt còn 100 ký tự. |
| `email` | Tùy chọn. Khi nhập, có phần tên, dấu @ và tên miền với hậu tố 2–63 chữ cái. Tối đa 100 ký tự sau chuẩn hóa. Kiểm tra trùng không phân biệt hoa thường. |
| `phone` | Bắt buộc. Sau khi bỏ dấu cách, chấm, ngoặc và gạch ngang, cần tiền tố `0` hoặc `+84` và 9–10 chữ số tiếp theo. |
| `address` | Tùy chọn, cắt còn tối đa 200 ký tự sau chuẩn hóa. |

Trường văn bản được loại khoảng trắng hai đầu và gộp khoảng trắng liên tiếp. Mật khẩu được so sánh nguyên văn. Thành công trả 302 đến `/login?success=registered`. Dữ liệu sai hoặc trùng được phát hiện trước khi lưu trả 400 và hiển thị lỗi. DAO lưu thất bại trả 409.

## Đăng nhập

- `GET /login` hiển thị form hoặc chuyển phiên đã đăng nhập đến `/dashboard`. `POST /login` nhận `username` và `password`.
- Đăng nhập đúng bằng tài khoản hoạt động đổi mã phiên và chuyển đến `/dashboard`. Dashboard chuyển khách hàng đến `/customer/orders`.
- Tên sai định dạng hoặc mật khẩu thiếu, toàn khoảng trắng hay vượt 72 byte trả 302 về `/login` với thông báo thông tin không hợp lệ. Sai thông tin đăng nhập hoặc tài khoản ngừng hoạt động cũng trả 302 và hiển thị thông báo đăng nhập thất bại.
- Sau 5 lần nhập sai thông tin đăng nhập hợp lệ về định dạng từ cùng IP và tên đăng nhập, lần tiếp theo bị tạm khóa trong 5 phút kể từ lúc đạt ngưỡng. Phản hồi có header `Retry-After`.

## Quản lý sản phẩm

| Thao tác | URL và dữ liệu | Kết quả |
| --- | --- | --- |
| Danh sách | `GET /staff/products`, có thể truyền `q` | Danh sách cả sản phẩm đang bán và ngừng bán. Tìm theo tên hoặc hãng. |
| Thêm | `POST /staff/products` với `action=add` | Tạo sản phẩm đang bán. Thành công chuyển đến `?success=add`. |
| Sửa | `POST /staff/products` với `action=update` và `productId` | Cập nhật sản phẩm hiện có và giữ số lượng tồn kho. Thành công chuyển đến `?success=update`. |
| Ngừng bán hoặc mở lại | Sửa sản phẩm, đặt `isActive=false` hoặc `true` | Ngừng bán ẩn sản phẩm khỏi danh mục và URL chi tiết công khai. Sản phẩm vẫn xuất hiện trong danh sách nhân viên để có thể mở bán lại. |
| Chi tiết công khai | `GET /product?id=...` | Trả 200 cho sản phẩm đang bán. ID thiếu, sai, không tồn tại hoặc ngừng bán trả 404. |

- `catId` phải là ID dương của danh mục tồn tại. `productName` dài 2–200 ký tự sau chuẩn hóa và cắt độ dài.
- `price` từ 0 đến 1.000.000.000 đồng, bao gồm hai đầu. Giá âm, vượt giới hạn hoặc không phải số bị từ chối.
- `stockQty` bắt buộc khi thêm, là số nguyên từ 0 đến 100.000. Việc sửa sản phẩm bỏ qua trường này.
- `brand`, `model`, `description` và `specs` là tùy chọn. Controller cắt còn lần lượt 80, 100, 2.000 và 1.000 ký tự.
- Yêu cầu CRUD sai dữ liệu, sai `action` hoặc ID cập nhật không tồn tại trả 302 về `/staff/products?error=invalid`.
