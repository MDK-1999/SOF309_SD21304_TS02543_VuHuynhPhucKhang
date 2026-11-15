# Nhật ký AI — Unit Test Controller GearAstral

**SOF309 · Tuần 2 (Y3) · Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

## 1. Ngữ cảnh và mục tiêu

Codex đọc [đặc tả](../SPECIFICATIONS.md), [test case Y2](../TEST_CASES.md), `RegisterServlet`, `LoginServlet`, `ProductServlet` và `InputValidator`. Mục tiêu là kiểm tra các nhánh xử lý tại Servlet và đối chiếu dữ liệu được gửi xuống DAO.

## 2. Prompt

Yêu cầu trong cuộc trò chuyện là “làm đi tôi mở sẵn gg trankhang có github rồi đó”, kèm ảnh hướng dẫn Tuần 2. Nội dung cần thực hiện theo ảnh gồm viết unit test JUnit 5 cho Controller, dùng `@ParameterizedTest` với `@CsvSource` để kiểm tra biên form, dùng `assertThrows()` cho ngoại lệ nghiệp vụ và đặt `@Timeout` 2 giây cho một luồng xử lý. Sau đó chạy Maven và tạo commit trên GitHub.

## 3. Đầu ra do AI tạo

- Kiểm tra biên tên và mật khẩu (REG-02/03/06/07), tên hoặc email trùng (REG-05/11), lỗi lưu và các phản hồi đăng nhập (LOGIN-01/02/04/07). [AuthControllerTest.java](../../src/test/java/com/poly/sof309/controllers/AuthControllerTest.java)
- Kiểm tra biên tên, giá, tồn kho (PROD-05/07/08/09), chuyển dữ liệu cho JSP và cập nhật sản phẩm giữ nguyên tồn kho trong giới hạn 2 giây (PROD-02/03). [ProductControllerTest.java](../../src/test/java/com/poly/sof309/controllers/ProductControllerTest.java)
- Mockito giả lập DAO, request, response và session. Các ca Controller gọi `service()` của Servlet thật và kiểm tra chuyển hướng, mã HTTP, thông báo hoặc dữ liệu gửi đến DAO.

## 4. Điều chỉnh khi đối chiếu mã nguồn

- Dùng biên tên đăng nhập 4–32 ký tự và giới hạn bcrypt 72 byte UTF-8 theo GearAstral.
- Tách kiểm tra trùng tài khoản vào `RegistrationService`. Test kiểm tra ngoại lệ tại service và kiểm tra Servlet vẫn hiển thị lỗi HTTP 400. Lỗi lưu qua DAO tiếp tục trả HTTP 409.
- Giữ constructor không tham số cho Servlet container và thêm constructor nhận phụ thuộc để test chạy độc lập với Tomcat và SQL Server.

## 5. Lệnh nghiệm thu

```powershell
mvn test "-Dtest=*ControllerTest"
```

Kết quả trên máy Windows với JDK 21: `BUILD SUCCESS`, 40 ca Controller, 0 failure, 0 error, 0 skipped. Lệnh `mvn test` cũng thành công và bỏ qua một ca Selenium mẫu đã có `@Disabled`.

[Nhật ký thiết kế kiểm thử Tuần 1](01_test_design_ai.md).
