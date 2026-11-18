# 05 – Nhật ký AI kiểm tra GitHub Actions GearAstral

## 1. Ngữ cảnh và yêu cầu

Repo `MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang` dùng workflow từ template môn SOF309. Người dùng yêu cầu kiểm tra code trước và sau khi đăng GitHub, đối chiếu kết quả với bài Tuần 2.

## 2. Đầu vào và đầu ra

Đầu vào gồm [workflow test.yml](../../.github/workflows/test.yml), pom.xml và bộ test. AI kiểm tra lệnh Maven, phiên bản JDK, báo cáo và kết quả run gắn đúng commit.

## 3. Cấu hình đang dùng

| Thành phần | Giá trị |
| --- | --- |
| Trigger | push và pull_request vào main/master, workflow_dispatch |
| Runner | ubuntu-latest |
| JDK | Temurin 17 qua actions/setup-java |
| Lệnh | mvn clean test |
| Artifact | test-reports từ target/surefire-reports/ |

Unit Test dùng Mockito nên job Y3 chạy độc lập với Tomcat và SQL Server. Ca LoginTest Selenium mẫu đang có `@Disabled`. Muốn nghiệm thu Y4 trên CI cần khởi động GearAstral, chuẩn bị fixture và bật các test UI đã tích hợp.

## 4. Bằng chứng đã xác nhận

Commit `214fed9474b62e9d639aa0e51c78f55e219f1624` đã lên main với 11 file của Tuần 2. Workflow CI Automation Test có conclusion `success`. [Xem run Tuần 2](https://github.com/MDK-1999/SOF309_SD21304_TS02543_VuHuynhPhucKhang/actions/runs/43726837327).

## 5. Kiểm tra mỗi lần cập nhật

Xác nhận SHA trên main, mở run có cùng SHA, đọc số failure/error/skipped và tải artifact khi còn thời hạn lưu. Các run bổ sung sau lần chỉnh bố cục cần được kiểm tra theo đúng commit mới.

## 6. Kiểm tra bản chỉnh bố cục ngày 18/11/2026

Lệnh `mvn clean verify` trên máy đã đạt. Hai class GearAstral chạy 40 ca đạt, hai ca mẫu cũng đạt và một ca Selenium mẫu bị bỏ qua. Tài liệu bổ sung URL, vai trò, fixture, truy vết và ca mở rộng dựa trên mã nguồn GearAstral. POM bổ sung JSTL API để WAR có thư viện phục vụ JSP.
