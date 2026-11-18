# 04 – Nhật ký AI phân tích lỗi GearAstral

## 1. Ngữ cảnh

Ghi các lỗi đã gặp khi tích hợp web và chạy Unit Test, cùng cách xác nhận lại. Người thực hiện AI là Codex. Sinh viên Vũ Huỳnh Phúc Khang, TS02543, SD21304.

## 2. Yêu cầu xử lý

Người dùng đã yêu cầu sửa triệt để lỗi Eclipse/Tomcat và kiểm tra đầy đủ bài Tuần 2 trước khi đăng GitHub.

## 3. Lỗi khởi động web

| Triệu chứng đã quan sát | Nguyên nhân được đối chiếu | Cách xử lý và bằng chứng |
| --- | --- | --- |
| Tomcat không publish cấu hình | Hai project cùng context /GearAstral | Bản SOF309 được đặt context /GearAstral-SOF309. Eclipse đã publish được. |
| Port 8080 already in use | Jetty và Tomcat cùng dùng 8080 trong phiên chạy trước | Đã thử Tomcat 8081, sau đó chuyển lại 8080 theo yêu cầu người dùng. Tomcat đã phục vụ GET /GearAstral-SOF309/shop trả 200 ngày 12/11. |
| NoClassDefFoundError với ConditionalTagSupport | Deployment thiếu JSTL API | Thêm jakarta.servlet.jsp.jstl-api 3.0.2. Bản build trong lần đối chiếu này khai báo rõ dependency để WAR có đủ thư viện. |

## 4. Lỗi khi thiết lập JUnit và Mockito

Mockito mặc định trên JDK 21 thử tự gắn Java agent. Phiên chạy Windows báo lỗi mở pipe attach, khiến lượt chạy dừng chờ.

Thiết lập hai file dưới `src/test/resources/mockito-extensions/`.

| File | Nội dung |
| --- | --- |
| org.mockito.plugins.MockMaker | mock-maker-subclass |
| org.mockito.plugins.MemberAccessor | member-accessor-reflection |

Sau cấu hình, hai class Controller GearAstral chạy 40 lượt đạt, 0 failure, 0 error, 0 skipped. Cách cấu hình này được áp dụng trong code đang nghiệm thu.

## 5. Lỗi khi đẩy code

Git Credential Manager trong phiên công cụ báo Access is denied. Code Tuần 2 được tải lên bằng GitHub Upload files trong Chrome Trankhang. Commit remote tạo ra là `214fed9` và [CI đã thành công](https://github.com/MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang/actions/runs/43726837327).

## 6. Tiêu chí kiểm tra sau sửa

Chạy lại bộ Controller và build WAR. Kiểm tra dependency JSTL trong `WEB-INF/lib` của WAR, đường dẫn tài liệu và dữ liệu biên. Lượt UI, thanh toán và giao dịch DB mở rộng dùng trạng thái theo Test Cases.

Lượt `mvn clean verify` ngày 18/11/2026 thành công. Báo cáo có 20 ca AuthControllerTest, 20 ca ProductControllerTest, 2 ca SampleControllerTest và 1 ca LoginTest bị bỏ qua. Tổng 43 được phát hiện, 0 failure, 0 error, 1 skipped. WAR được tạo tại `target/GearAstral.war`.
