# Kế hoạch kiểm thử GearAstral

**SOF309 · Tuần 1 · Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

## Mục tiêu và phạm vi

Nghiệm thu web chạy trên cổng 8080, đăng ký khách hàng, đăng nhập theo vai trò và quản lý sản phẩm. Với sản phẩm, kiểm tra thêm, đọc, sửa, ngừng bán và mở lại. Kiểm tra trạng thái HTTP, URL chuyển hướng, nội dung JSP và dữ liệu quan sát được sau thao tác. Các ca UI kiểm tra biểu mẫu, thông báo và thao tác trong cửa sổ thêm hoặc sửa sản phẩm.

Thanh toán, đặt hàng, tư vấn AI, ảnh sản phẩm từ nguồn ngoài và SQL Server nằm ngoài lượt nghiệm thu này.

## Môi trường và dữ liệu

- JDK 17, Maven, Jetty 12 và H2 demo chạy ở `http://localhost:8080/`.
- Dùng ba phiên cookie độc lập cho khách, khách hàng và nhân viên `demostaff`. Dùng tài khoản mới ở mỗi lần đăng ký thành công.
- Đăng ký bằng dữ liệu giả lập và địa chỉ `example.test`. Dùng danh mục SSD, đặt tên sản phẩm có hậu tố riêng để tìm lại dễ dàng.
- Với yêu cầu POST trực tiếp, lấy `_csrf` qua GET trước rồi giữ cookie cùng phiên. Chỉ bỏ hoặc thay token trong ca kiểm thử CSRF.
- Bắt đầu từ dữ liệu demo sạch. Chạy P0 trước. Với thao tác bị từ chối, đọc lại bảng sản phẩm hoặc đăng nhập lại để kiểm tra dữ liệu gốc.

## Thiết kế kiểm thử

| Nghiệp vụ | Phân vùng tương đương | Phân tích biên |
| --- | --- | --- |
| Đăng ký | Tên đúng, sai ký tự, trùng. Mật khẩu đủ nhóm ký tự hoặc thiếu, xác nhận khớp hoặc lệch. Email trống, đúng, sai hoặc trùng. Điện thoại đúng hoặc sai tiền tố. | Tên 3/4/5 và 31/32/33. Mật khẩu 7/8/9 ký tự và 71/72/73 byte. Họ tên 1/2/3 và 99/100/101. Điện thoại có 8/9/10/11 chữ số sau tiền tố. |
| Đăng nhập và quyền | Tài khoản đúng, sai, ngừng hoạt động. Khách, khách hàng và nhân viên. CSRF đúng, thiếu hoặc sai. | Quan sát lần sai thứ 4, 5 và lần thử thứ 6. |
| Sản phẩm | Tạo hợp lệ, sai danh mục, giá hoặc tồn kho không phải số, cập nhật tồn tại hoặc không tồn tại, đang bán hoặc ngừng bán. | Tên 1/2/3 và 199/200/201. Giá -1/0/1 và 999.999.999/1.000.000.000/1.000.000.001. Tồn kho -1/0/1 và 99.999/100.000/100.001. |

## Tiêu chí nghiệm thu

- `mvn clean package` thành công. `/shop`, `/register`, `/login` và danh sách sản phẩm truy cập đúng theo vai trò.
- Các luồng P0 được chạy thử qua HTTP và kết quả thực tế được ghi trong [bộ test case](TEST_CASES.md).
- Mỗi ca thiết kế có dữ liệu, bước thực hiện và kết quả mong đợi. Ca UI và các bộ dữ liệu chưa chạy được ghi rõ.
