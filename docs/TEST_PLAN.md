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

**Phạm vi tự động Tuần 2** gồm 40 lượt kiểm thử Controller GearAstral bằng JUnit 5 và Mockito. Bộ test gọi Servlet qua `service()`, kiểm tra trạng thái, thông báo, chuyển hướng và dữ liệu truyền xuống DAO. Danh mục sản phẩm, giỏ hàng, giao dịch kho, vai trò admin và UI có ca thiết kế để mở rộng theo tiến độ.

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

## 4. DỮ LIỆU CHUẨN VÀ MA TRẬN QUYỀN

### 4.1. Fixture cho mỗi lượt

| Fixture | Dữ liệu | Cách cô lập |
| --- | --- | --- |
| REG | Tên `gearqa`, mật khẩu `Gear1234`, họ tên GearAstral QA, email `qa@example.test`, điện thoại `0900000000` | Mockito tạo request, response và UserDAO mới trong `@BeforeEach`. |
| LOGIN | UserDAO trả User có tên GearAstral QA hoặc trả null, IP tài liệu `192.0.2.10` | `@AfterEach` xóa lượt sai theo khóa IP và username. |
| PROD | `catId=1`, tên SSD, giá 100, tồn 5 | ProductDAO và CategoryDAO mới. `ArgumentCaptor` đọc Product gửi xuống DAO. |
| UPDATE | ID 25, tồn 5, đang bán | DAO trả Product riêng của test. Gửi `stockQty=99999` khi sửa để xác nhận tồn vẫn bằng 5. |
| HTTP | H2 demo tại localhost:8080, khách, customer và demostaff | Tạo dữ liệu với hậu tố lượt chạy. Mỗi vai trò giữ cookie và CSRF riêng. |
| ADMIN | Tài khoản admin trong DB kiểm thử, sản phẩm và danh mục riêng | Seed trước khi chạy các ca quản trị. Kiểm tra ID thực tồn tại. |
| ORDER | Sản phẩm còn hàng, giá kiểm soát được và giỏ riêng | Dùng DB riêng, đối chiếu đơn, chi tiết và tồn kho sau thao tác. |

### 4.2. Ma trận đường dẫn

| Vai trò | Công khai | /customer/orders | /staff/products, /staff/stock, /staff/orders | /admin/categories, /admin/users, /admin/report |
| --- | --- | --- | --- | --- |
| Khách | Cho phép | 302 tới login | 302 tới login | 302 tới login |
| customer | Cho phép | Cho phép, kiểm tra chủ đơn | 302 tới dashboard với error=forbidden | 302 tới dashboard với error=forbidden |
| staff | Cho phép | 302 tới dashboard với error=forbidden | Cho phép | 302 tới dashboard với error=forbidden |
| admin | Cho phép | 302 tới dashboard với error=forbidden | Cho phép | Cho phép |

Mỗi ô bảo vệ được kiểm tra với GET và POST mà route hỗ trợ. POST có token hợp lệ khi kiểm quyền. Ca CSRF dùng sai hoặc thiếu token để xác nhận 403 trước kiểm tra nghiệp vụ.

## 5. MÔI TRƯỜNG VÀ CÁCH THỰC THI

| Tầng | Công cụ và điều kiện | Đầu ra |
| --- | --- | --- |
| Unit Controller | JUnit 5, Mockito, JDK 17 trở lên, Maven | XML và TXT Surefire |
| HTTP và tích hợp | Jetty 12/H2 demo hoặc Tomcat 10.1 và DB kiểm thử | URL, trạng thái, chuyển hướng, nội dung và dữ liệu đọc lại |
| UI | Selenium 4, Chrome, Page Object Model, WebDriverWait | Bước thao tác, locator, kết quả hiển thị và ảnh lỗi khi triển khai listener |
| CI | Ubuntu, Temurin 17, `mvn clean test` | Run URL và artifact test-reports |

```powershell
mvn test "-Dtest=AuthControllerTest,ProductControllerTest"
mvn test "-Dtest=*ControllerTest"
mvn clean package
```

Lệnh đầu chạy hai class GearAstral với 40 lượt. Lệnh wildcard chạy thêm `SampleControllerTest` của template. `mvn test` chạy thêm ca Selenium mẫu đang có `@Disabled`. Người nghiệm thu đọc riêng số skipped và tên class trong báo cáo.

## 6. TRUY VẾT TEST CASE VỚI CODE

| Nghiệp vụ | ID thiết kế | Phương thức kiểm thử |
| --- | --- | --- |
| Tên đăng nhập | REG-02, REG-03 | `AuthControllerTest.validatesUsernameLength` |
| Mật khẩu và byte UTF-8 | REG-06, REG-07 | `AuthControllerTest.validatesPasswordLength` |
| Trùng tên, email | REG-05, REG-11 | `AuthControllerTest.rejectsDuplicateAccount` |
| Đăng nhập và phiên | LOGIN-01, LOGIN-02, LOGIN-04, LOGIN-07 | `successfulLoginRotatesSession`, `invalidCredentialsReturnToLogin`, `missingPasswordSkipsLookup` |
| Biên form sản phẩm | PROD-05, PROD-07, PROD-08, PROD-09 | `ProductControllerTest.validatesAddForm` |
| Cập nhật và ngừng bán | PROD-02, PROD-03 | `ProductControllerTest.updatesProductAndPreservesStock` |
| Kết quả tìm kiếm | PROD-READ-01 | `ProductControllerTest.forwardsSearchResultsToProductView` |

Các ID có nhiều dữ liệu phải ghi đúng các giá trị đã chạy. Một dataset pass không thay thế kết quả của toàn bộ họ ca hoặc tích hợp DB.

## 7. TIÊU CHÍ VÀO, RA VÀ QUẢN LÝ LỖI

* Trước khi chạy xác nhận branch, commit, phiên bản Java, dữ liệu chuẩn và tầng test.
* Bộ Y3 đạt khi 40 ca GearAstral pass, 0 failure, 0 error, 0 skipped và có `@ParameterizedTest`, `@CsvSource`, `assertThrows`, `@Timeout`.
* Ca tích hợp đạt khi phản hồi và dữ liệu sau thao tác khớp đặc tả. Thiếu DB hoặc tài khoản ghi Blocked cùng điều kiện cần bổ sung.
* Ca UI đạt khi trình duyệt chạy trên form GearAstral và kiểm tra kết quả hiển thị. Ca đang có `@Disabled` được ghi Chưa chạy.
* Mỗi lỗi ghi ID, bước tái hiện, dữ liệu, expected, actual, tầng ảnh hưởng, commit sửa và kết quả chạy lại.
* Báo cáo Maven lưu tại `target/surefire-reports/`. CI lưu artifact `test-reports`. Nhật ký lỗi tại [04_debug_logs_ai.md](ai-logs/04_debug_logs_ai.md).

## 8. MINH CHỨNG VÀ TIẾN ĐỘ

Lượt Y3 của commit `214fed9` đã được GitHub Actions xác nhận thành công. Bảng test case ghi riêng kết quả HTTP lịch sử, Unit Test và các phần chờ thực thi. Các tài liệu AI 01–05 có mục lục và trạng thái tại [ai-logs/README.md](ai-logs/README.md).

Ngày đối chiếu kế hoạch 18/11/2026.
