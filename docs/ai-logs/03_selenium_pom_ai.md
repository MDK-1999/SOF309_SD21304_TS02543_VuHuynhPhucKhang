# 03 – Nhật ký AI thiết kế Selenium Page Object Model

## 1. Ngữ cảnh và mục tiêu

Dự án GearAstral của Vũ Huỳnh Phúc Khang, TS02543, SD21304. Ngày đối chiếu 18/11/2026. Đầu vào gồm các JSP đăng nhập, đăng ký, sản phẩm, lớp LoginPage và LoginTest của template.

Mục tiêu của phiên này là xác định cấu trúc và locator cần dùng cho Y4, ghi tiến độ chính xác khi sắp xếp repo theo bố cục mẫu.

## 2. Yêu cầu đầu vào

> Làm lại GitHub dựa vào GearAstral, repository được gửi chỉ là mẫu.

## 3. Kết quả phân tích từ JSP

| Page Object | URL | Locator được đối chiếu | Hành động dự kiến |
| --- | --- | --- | --- |
| LoginPage | /login | `#loginUsername`, `#loginPassword`, `form[action$='/login'] button[type='submit']` | Nhập thông tin, gửi form, chờ URL hoặc toast. |
| RegisterPage | /register | `#fullName`, `#username`, `#password`, `#confirm`, `#email`, `#phone`, `#address` | Tạo customer mới với dữ liệu riêng theo runId. |
| ProductPage | /staff/products | `#ptbl`, `tr[data-id]`, form trong modal sản phẩm | Đọc bảng, mở form thêm/sửa, lưu và kiểm tra dữ liệu. |

Toast đăng nhập dùng `#toast .toast-title` và `#toast .toast-msg`. Form chứa `_csrf` do server cấp cho session. Kịch bản Selenium gửi form bình thường và dùng WebDriverWait cho kết quả.

## 4. Hiện trạng và phần cần thực hiện

- Repo có LoginPage và LoginTest mẫu. Locator mẫu `username/password` cần đổi thành `loginUsername/loginPassword` cho trang đăng nhập GearAstral.
- LoginTest hiện có `@Disabled`. Chưa có kết quả chạy Selenium trên form GearAstral.
- Khi triển khai Y4 cần viết RegisterPage, ProductPage, fixture tài khoản mới, cấu hình base URL và các test đăng ký, đăng nhập, CRUD.
- TestNG, groups, DataProvider và listener chụp ảnh lỗi thuộc bước nâng cấp theo hướng dẫn môn học.

## 5. Cách nghiệm thu dự kiến

Khởi động H2 demo, chạy UI trên Chrome với đúng context, xác nhận dữ liệu sau thao tác và lưu kết quả. Báo cáo mỗi ca phải có trạng thái thực tế. Nhật ký này là đầu ra thiết kế của phiên đối chiếu.
