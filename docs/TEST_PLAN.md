# KẾ HOẠCH KIỂM THỬ (TEST PLAN)

### Dự án: GearAstral

> **Sinh viên:** Vũ Huỳnh Phúc Khang · TS02543 · SD21304<br>
> **Tham chiếu:** Slide 1 (Kiểm thử đơn vị), Lab 1 (Tổng quan kiểm thử phần mềm) và [đặc tả GearAstral](SPECIFICATIONS.md).

---

## 1. MỤC TIÊU KIỂM THỬ (TEST OBJECTIVES - BÀI 2 LAB 1)

| STT | Mục tiêu kiểm thử | Tầm quan trọng trong thực tế | Hoạt động kiểm thử cụ thể | Phân loại |
| :---: | :--- | :--- | :--- | :---: |
| **1** | **Xác thực đăng nhập, đăng ký và phân quyền** | Bảo vệ tài khoản và khu vực quản lý của nhân viên. | Kiểm tra dữ liệu hợp lệ và sai định dạng, tên và email trùng, quyền truy cập và kết thúc phiên. | **Verification** |
| **2** | **Kiểm tra đặt hàng và trạng thái thanh toán** | Đảm bảo nhân viên theo dõi đúng đơn COD và trạng thái thanh toán. | Thiết kế ca giỏ trống, đặt hàng COD và cập nhật trạng thái hợp lệ hoặc sai. Thực thi ở đợt mở rộng. | **Verification** |
| **3** | **Đảm bảo dữ liệu CRUD sản phẩm nhất quán** | Giá và tồn kho phải hợp lệ. Sản phẩm ngừng bán phải được ẩn với khách. | Kiểm tra thêm, đọc, sửa, ngừng bán, mở bán lại và biên tên, giá, tồn kho. | **Verification** |
| **4** | **Kiểm tra trải nghiệm trên giao diện Web** | Người dùng cần thấy thông báo phù hợp và thao tác được trên các biểu mẫu chính. | Dùng Selenium để kiểm tra đăng ký, đăng nhập và lưu hoặc hủy sửa sản phẩm. | **Validation** |
| **5** | **Chạy kiểm thử hồi quy trên GitHub Actions** | Phát hiện lỗi sớm khi cập nhật mã nguồn. | Workflow chạy `mvn clean test` trên JDK 17 và lưu báo cáo Surefire. Mở rộng bộ test theo tiến độ của môn. | **Verification** |

**Phạm vi Tuần 1** gồm đăng ký, đăng nhập, phân quyền, quản lý sản phẩm và smoke test HTTP. Đặt hàng, thanh toán và Selenium thuộc đợt mở rộng. Lượt nghiệm thu dùng H2 demo, với dữ liệu giả lập, tài khoản mới và phiên riêng cho khách, khách hàng và nhân viên.

---

## 2. PHÂN BIỆT KIỂM THỬ VÀ GỠ LỖI (TESTING VS DEBUGGING - BÀI 1 LAB 1)

* **Kiểm thử (Testing):** Chạy các ca theo đặc tả và so sánh kết quả thực tế với kết quả mong đợi. Ví dụ, gửi giá `-1` khi thêm sản phẩm, kiểm tra thông báo lỗi và xác nhận bản ghi không được thêm.
* **Gỡ lỗi (Debugging):** Nếu ca kiểm thử thất bại, tái hiện bằng cùng bộ dữ liệu, kiểm tra luồng từ `ProductServlet` đến validation và DAO, sửa nguyên nhân rồi chạy lại ca đã lỗi cùng các ca biên liên quan.

---

## 3. CHIẾN LƯỢC KIỂM THỬ ĐƠN VỊ (UNIT TEST STRATEGY - SLIDE 1)

* **Nguyên tắc độc lập:** Mỗi ca JUnit tự tạo dữ liệu hoặc fixture bằng `@BeforeEach`. Dùng tên riêng cho các ca tạo mới. Với kịch bản HTTP nhiều bước, tạo dữ liệu chuẩn bị và giữ cookie cùng phiên.
* **Bộ thư viện:** JUnit 5 (`@Test`, `@BeforeEach`, `@DisplayName`). Dùng `@ParameterizedTest` và `@CsvSource` cho các bộ dữ liệu biên. Dùng Selenium WebDriver 4 cho ca UI.
* **Assertions:** Dùng `assertEquals` cho giá trị và trạng thái, `assertTrue` hoặc `assertFalse` cho điều kiện hợp lệ, `assertNotNull` cho kết quả cần tồn tại. Dùng `assertThrows` khi hợp đồng của phương thức yêu cầu ném ngoại lệ.
* **Môi trường:** JDK 17, Maven, Jetty 12 và H2 demo tại `http://localhost:8080`. Trước POST hợp lệ, lấy token CSRF qua GET và giữ cùng cookie phiên.
* **Phân vùng tương đương và phân tích giá trị biên:**

| Nghiệp vụ | Phân vùng tương đương | Phân tích giá trị biên |
| :--- | :--- | :--- |
| Đăng ký | Tên đúng, sai ký tự hoặc trùng. Mật khẩu đủ nhóm ký tự hoặc thiếu, xác nhận khớp hoặc lệch. Email trống, đúng, sai hoặc trùng. Điện thoại đúng hoặc sai tiền tố. | Tên 3/4/5 và 31/32/33 ký tự. Mật khẩu 7/8/9 ký tự và 71/72/73 byte. Họ tên 1/2/3 và 99/100/101 ký tự. Điện thoại có 8/9/10/11 chữ số sau tiền tố. |
| Đăng nhập và quyền | Tài khoản đúng, sai hoặc ngừng hoạt động. Khách, khách hàng hoặc nhân viên. CSRF đúng, thiếu hoặc sai. | Quan sát lần sai thứ 4, thứ 5 và lần thử thứ 6. |
| Sản phẩm | Dữ liệu hợp lệ, sai danh mục, giá hoặc tồn kho sai kiểu, cập nhật ID hợp lệ hoặc sai, sản phẩm đang bán hoặc ngừng bán. | Tên 1/2/3 và 199/200/201 ký tự. Giá -1/0/1 và 999.999.999/1.000.000.000/1.000.000.001 đồng. Tồn kho -1/0/1 và 99.999/100.000/100.001. |
| Đặt hàng và thanh toán | Giỏ trống hoặc có hàng. Trạng thái thanh toán hợp lệ hoặc ngoài danh sách cho phép. | Các ca của đợt mở rộng tập trung vào phân vùng. |

* **Tiêu chí kết thúc:** `mvn clean test` thành công và 100% ca JUnit trong bộ test đang chạy đạt PASS. Đợt nghiệm thu HTTP kiểm tra các luồng chính tại cổng 8080. Kết quả thực tế và các ca còn chờ thực thi được ghi trong [bộ kịch bản kiểm thử](TEST_CASES.md).
