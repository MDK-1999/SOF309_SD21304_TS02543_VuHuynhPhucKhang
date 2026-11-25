# 03 – Nhật ký AI thiết kế và triển khai Selenium Page Object Model

**SOF309 · Tuần 3 (Y4 – Phần 1) · GearAstral · Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

## 1. Mục tiêu

Tự động hóa các luồng đăng nhập, đăng ký và quản lý sản phẩm trên Chrome. Locator và thao tác DOM nằm trong Page Object. Kịch bản trong `tests/` chỉ điều phối các thao tác và xác nhận kết quả nghiệp vụ.

> Dựa trên các JSP và Test Cases của GearAstral để triển khai Page Object và JUnit 5. Dùng WebDriverWait cho các thao tác cần chờ. Tạo dữ liệu kiểm thử riêng trên H2 demo và xác nhận kết quả khi chạy bằng Chrome.

## 2. Page Object và thiết kế chờ

| Page Object | Trang hoặc phần dùng chung | Trách nhiệm |
| --- | --- | --- |
| `BasePage` | Dùng chung | Mở đường dẫn, điền trường, chờ phần tử hiển thị hoặc bấm được và chờ URL chính xác. |
| `LoginPage` | `/login` | Điền `loginUsername`, `loginPassword`, gửi form và đọc tiêu đề thông báo. |
| `RegisterPage` | `/register` | Điền các trường đăng ký và đọc lỗi validation được hiển thị. |
| `ProductPage` | `/staff/products` | Mở form thêm/sửa, chọn danh mục, lưu, hủy, ngừng bán và đọc bảng. |
| `StorefrontPage` | `/product?id=...` | Đọc tên sản phẩm công khai hoặc mã lỗi hiển thị khi sản phẩm ngừng bán. |

`WebDriverWait` chờ trường, nút, thông báo, URL hoặc việc form cũ biến mất khỏi DOM sau khi gửi. Test không sử dụng `Thread.sleep()`. Các form được gửi bằng thao tác thật của Chrome, giữ phiên và CSRF như người dùng thông thường.

## 3. Các ca đã chạy

| Kịch bản | Điều cần xác nhận | Kết quả |
| --- | --- | --- |
| `LoginTest` · UI-LOGIN-01 | Staff đăng nhập đúng, về dashboard và mở được trang sản phẩm. | PASS |
| `LoginTest` · UI-LOGIN-02 | Mật khẩu sai hiển thị thông báo và giữ trang đăng nhập. | PASS |
| `RegisterTest` · UI-REGISTER-01 | Tạo customer với dữ liệu riêng, thấy thông báo và đăng nhập được bằng tài khoản mới. | PASS |
| `RegisterTest` · UI-REGISTER-02 | Xác nhận mật khẩu khác bị từ chối và hiển thị lỗi. | PASS |
| `CrudTest` · UI-PROD-01 | Thêm, đọc, hủy sửa, cập nhật giá/tên, giữ tồn kho và ngừng bán. Chi tiết công khai trả 404 sau khi ngừng bán. | PASS |

## 4. Chạy và đối chiếu kết quả

Mở GearAstral H2 demo với `mvn -Pdemo jetty:run`, rồi chạy `mvn -Pui test` trong terminal thứ hai. Xem [README](../../README.md) để chọn cổng hoặc chỉ định ChromeDriver khi cần. Các tài khoản và sản phẩm thử có dữ liệu riêng, được đặt lại khi dừng và mở lại web demo.

Ngày 22/11/2026, bộ test chạy thành công trên Windows 11, JDK 17, Jetty/H2, Chrome và ChromeDriver `157.0.8103.80`, ở chế độ Chrome có cửa sổ. Kết quả `5 tests, 0 failures, 0 errors, 0 skipped`. Lệnh `mvn test` cũng đạt `42 tests, 0 failures, 0 errors, 0 skipped` cho bộ controller.

Selenium 4.20.0 ghi cảnh báo về phiên bản Chrome DevTools Protocol. Các ca trên dùng thao tác WebDriver thông thường và đã chạy thành công. Báo cáo chi tiết ở `target/surefire-reports/`.
