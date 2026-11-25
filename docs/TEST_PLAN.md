# KẾ HOẠCH KIỂM THỬ (TEST PLAN)

## 1. Thông tin và mục tiêu

- **Dự án** GearAstral – Web bán linh kiện máy tính.
- **Sinh viên** Vũ Huỳnh Phúc Khang – TS02543 – SD21304.
- **Môn học** SOF309 – Kiểm thử nâng cao cùng AI.
- **Phiên bản kế hoạch** 2.0 – 18/11/2026.
- **Căn cứ** [SPECIFICATIONS.md](SPECIFICATIONS.md), mục 2 về vai trò, mục 3 về URL, mục 4 về nghiệp vụ và mục 5 về phiên, CSRF.
- **Bộ ca** [TEST_CASES.md](TEST_CASES.md).
- **Nhật ký thiết kế** [01_test_design_ai.md](ai-logs/01_test_design_ai.md).
- **Mục tiêu** Xác minh validation và phản hồi tại Controller/Service/Filter, quyền truy cập, tính đúng của giá và tồn kho, quyền sở hữu đơn hàng và luồng thao tác trên trình duyệt.

Hai class `AuthControllerTest` và `ProductControllerTest` đã chạy 40 ca đạt. Các ca HTTP có kết quả lịch sử theo từng dòng trong Test Cases. Bộ Selenium Tuần 3 chạy riêng bằng `mvn -Pui test` sau khi khởi động GearAstral H2 demo. Lần chạy 22/11/2026 trên Chrome đạt 5/5 ca, với kết quả từng ca trong Test Cases.

## 2. Phạm vi

**Trong phạm vi**

- Đăng ký, đăng nhập, đăng xuất, đổi mã phiên khi đăng nhập và khóa tạm thời sau nhiều lần nhập sai.
- Xem, tìm kiếm, lọc, sắp xếp, phân trang và xem chi tiết linh kiện.
- Thêm, xem, sửa, ngừng bán và mở bán lại sản phẩm.
- Giỏ hàng, cập nhật số lượng, xóa dòng và đặt hàng COD.
- Danh sách đơn, chi tiết đơn và quyền sở hữu đơn của customer.
- Nhập kho, tạo đơn tại khu vực nhân viên, cập nhật trạng thái đơn, trạng thái thanh toán và hóa đơn.
- Quản lý danh mục, tài khoản, báo cáo trong khu vực admin.
- PC Builder và hợp đồng phản hồi của chức năng tư vấn AI.
- Phân quyền customer/staff/admin, CSRF, session và dữ liệu hiển thị trên JSP.

**Ngoài phạm vi**

Kiểm thử tải lớn, hiệu năng chuyên sâu, đánh giá xâm nhập toàn diện, chứng nhận chất lượng mô hình AI, giao dịch tiền thật và tương thích toàn bộ thiết bị/trình duyệt.

**Giới hạn**

- Unit Test dùng DAO giả lập để kiểm tra luồng xử lý và dữ liệu truyền xuống DAO. Kiểm tra giao dịch, khóa ngoại và dữ liệu được lưu cần chạy với H2 hoặc SQL Server test.
- H2 demo khởi tạo lại dữ liệu sau mỗi lần chạy ứng dụng. Môi trường SQL Server cần có schema, fixture và tài khoản đúng vai trò.
- Các ca admin, kho, đơn hàng và AI chỉ được thực thi khi có đủ điều kiện tương ứng.
- Hành vi cắt chuỗi, giá sản phẩm bằng 0, fallback số lượng giỏ và fallback giá nhập được xác định theo mã hiện tại. Đề xuất thay đổi nghiệp vụ cần được xác nhận trước khi dùng làm tiêu chí nghiệm thu.

## 3. Chiến lược theo tầng

| Tầng | Cách thực hiện | Kết quả cần kiểm tra |
| --- | --- | --- |
| Controller | JUnit 5, Mockito, request/response/session và DAO được kiểm soát. Gọi `service()` của Servlet thật với GET/POST | HTTP status, forward/redirect, thông báo, session, dữ liệu và số lần gọi DAO |
| Service | Kiểm tra `RegistrationService` và validation bằng dữ liệu tham số | Ngoại lệ tài khoản trùng, thông báo chính xác và điều kiện gọi lưu |
| Filter | Gọi `SecurityFilter` và `AuthFilter` với phiên, CSRF, role được chuẩn bị | 403 khi CSRF sai, chuyển hướng khi thiếu phiên/sai quyền và số lần gọi filter chain |
| Tích hợp | Jetty/H2 demo hoặc Tomcat/SQL Server test, dữ liệu riêng cho mỗi lượt | Dữ liệu trước/sau, giao dịch đơn và chi tiết, trừ kho, rollback và quyền sở hữu |
| UI | Selenium WebDriver trên Chrome, Page Object Model, WebDriverWait | Nội dung, URL, validation form, toast, trạng thái modal và dữ liệu sau tải lại |
| Smoke | Chọn ca P0 cho đăng nhập, phân quyền, CSRF, thêm/sửa sản phẩm và đặt đơn COD | Các luồng thiết yếu hoạt động với dữ liệu chuẩn |
| Regression | Chạy lại bộ test đã sẵn sàng sau mỗi lần sửa, ưu tiên P0 rồi P1 | Lỗi cũ được xử lý và các luồng liên quan tiếp tục đúng |

Trong test Controller, dữ liệu có thể được gửi trực tiếp để kiểm tra validation máy chủ. Trong test UI, thao tác qua form và ghi đúng ràng buộc mà trình duyệt áp dụng.

`@ParameterizedTest` với `@CsvSource` dùng cho dữ liệu biên. `assertThrows()` kiểm tra `DuplicateAccountException`. `@Timeout(value = 2, unit = TimeUnit.SECONDS)` giới hạn ca cập nhật sản phẩm.

## 4. Kỹ thuật thiết kế

### 4.1. Phân vùng tương đương (EP)

| Đầu vào hoặc nghiệp vụ | Phân vùng hợp lệ | Phân vùng lỗi hoặc hành vi cần kiểm tra |
| --- | --- | --- |
| Trường bắt buộc | Có dữ liệu đáp ứng ràng buộc | Thiếu tham số, rỗng, toàn khoảng trắng |
| Tên đăng nhập | 4–32 ký tự ASCII thuộc chữ, số, dấu chấm, gạch dưới, gạch ngang | Sai ký tự, ngoài độ dài, trùng không phân biệt hoa thường |
| Mật khẩu đăng ký | Có hoa, thường, số, ít nhất 8 ký tự và tối đa 72 byte UTF-8 | Thiếu nhóm ký tự, xác nhận lệch, quá giới hạn byte |
| Email và điện thoại | Email trống hoặc đúng định dạng, số điện thoại Việt Nam đúng tiền tố và độ dài | Email sai/trùng, số điện thoại thiếu hoặc sai định dạng |
| Đăng nhập | Tài khoản hoạt động và mật khẩu đúng | Tên chưa có, mật khẩu sai, tài khoản ngừng hoạt động, đang bị khóa tạm thời |
| Số nguyên và ID | Đúng kiểu, nằm trong miền, ID tồn tại khi nghiệp vụ yêu cầu | Chữ, số thập phân, ngoài miền, ID không tồn tại |
| Sản phẩm | Danh mục hợp lệ, tên đủ dài, giá và tồn hợp lệ | Danh mục sai, giá/tồn sai kiểu, sản phẩm ngừng bán, ID cập nhật sai |
| Giỏ hàng | Sản phẩm đang bán, còn tồn, lượng trong miền | Hết hàng, ngừng bán, vượt tồn, lượng sai được xử lý theo fallback |
| Checkout | Giỏ có hàng, thông tin nhận hợp lệ, tồn kho đủ | Giỏ trống, dữ liệu nhận sai, giá/tồn thay đổi, lỗi lưu đơn |
| Quyền và chủ sở hữu | Đúng role và đơn thuộc customer hiện tại | Thiếu phiên, sai role, truy cập đơn của customer khác |
| Trạng thái đơn/thanh toán | Giá trị thuộc tập cho phép | Giá trị lạ, thiếu, ID đơn sai hoặc cập nhật thất bại |
| Phiên và CSRF | Phiên còn hiệu lực, token đúng phiên | Token thiếu, sai, token của phiên khác, phiên đã kết thúc |
| Tư vấn AI | Request hợp lệ, cấu hình provider đáp ứng | Provider chậm/lỗi, dữ liệu rỗng, vượt giới hạn lượt |

### 4.2. Phân tích giá trị biên (BVA)

Dùng min−1, min, min+1 và max−1, max, max+1 khi có hai biên. Mỗi lượt thay một trường, giữ các trường còn lại hợp lệ. Mỗi giá trị trong bảng là một dataset độc lập.

| Quy tắc GearAstral | Giá trị kiểm tra | Kết quả cần đối chiếu |
| --- | --- | --- |
| Username 4–32 | 3, 4, 5, 31, 32, 33 ký tự | Ngoài miền bị từ chối khi đăng ký |
| Password tối thiểu 8 ký tự | 7, 8, 9, có đủ nhóm ký tự | 7 bị từ chối, 8/9 qua điều kiện độ dài |
| Bcrypt tối đa 72 byte | 71, 72, 73 byte ASCII và UTF-8 đa byte | 73 byte bị từ chối, đo số byte riêng với số ký tự |
| Họ tên đăng ký 2–100 | 1, 2, 3, 99, 100, 101 ký tự | 1 bị từ chối, 101 được cắt còn 100 |
| Email đăng ký tối đa 100 sau chuẩn hóa | Email đúng định dạng dài 99, 100, 101 | 101 được cắt còn 100 rồi kiểm tra định dạng và trùng |
| Điện thoại 0/+84 và 9–10 chữ số tiếp theo | Mỗi tiền tố kết hợp 8, 9, 10, 11 chữ số | 8/11 bị từ chối, kiểm tra thêm dấu cách và chuẩn hóa |
| Địa chỉ đăng ký tối đa 200 | Rỗng, 199, 200, 201 ký tự | Tùy chọn, 201 được cắt còn 200 |
| Tên sản phẩm 2–200 | 1, 2, 3, 199, 200, 201 ký tự | 1 bị từ chối, 201 được cắt còn 200 |
| Giá sản phẩm 0–1.000.000.000 | -1, 0, 1, 999999999, 1000000000, 1000000001 | Hai đầu hợp lệ, ngoài miền bị từ chối |
| Tồn ban đầu 0–100.000 | -1, 0, 1, 99999, 100000, 100001 | Ngoài miền bị từ chối |
| Lượng giỏ 1–99, tồn S=5 | -1, 0, 1, 4, 5, 6, 98, 99, 100 | Giá trị parser không chấp nhận dùng fallback 1. Lượng hợp lệ được chặn theo S và 99 |
| Tên người nhận tối thiểu 2 | 1, 2, 3 ký tự | 1 bị từ chối khi không có thông tin tài khoản thay thế |
| Địa chỉ giao hàng 5–200 | 4, 5, 6, 199, 200, 201 ký tự | 4 bị từ chối, 201 được cắt còn 200 |
| Nhập kho 1–100.000 | 0, 1, 2, 99999, 100000, 100001 | Ngoài miền bị từ chối |
| Giá nhập 0–1.000.000.000 | -1, 0, 1, 999999999, 1000000000, 1000000001 | Giá trị sai dùng fallback 0 theo mã hiện tại |
| Tên danh mục 2–100 | 1, 2, 3, 99, 100, 101 ký tự | 1 bị từ chối, 101 được cắt còn 100 |
| Khóa đăng nhập sau 5 lần sai | Quan sát lần 4, 5 và lần thử 6 | Lần thử 6 bị chặn và có Retry-After khi thời gian khóa còn hiệu lực |
| Trang danh mục, tối đa 24 sản phẩm/trang | 0, 1, 2, trang cuối, trang cuối+1, 100001 | Parser dùng fallback khi sai miền. Trang hợp lệ vượt tổng trang được đưa về trang cuối |

Chuỗi độ dài dùng ký tự ASCII để đo rõ ràng. Các ca tiếng Việt và ký tự đa byte kiểm tra riêng khả năng hiển thị, chuẩn hóa và giới hạn UTF-8.

### 4.3. Tổ hợp phân quyền bắt buộc (ROLE_MATRIX)

Mỗi endpoint và phương thức mà route hỗ trợ là một lượt riêng. A là được đi tiếp qua AuthFilter. L là chuyển tới `/login?error=session`. F là chuyển tới `/dashboard?error=forbidden`.

| Endpoint/phương thức | Khách | customer | staff | admin |
| --- | --- | --- | --- | --- |
| GET /shop, /product, /product-image | A | A | A | A |
| GET/POST /cart, /checkout, /pc-builder | A | A | A | A |
| POST /ai-chat | A | A | A | A |
| GET /dashboard | L | A | A | A |
| POST /logout | L | A | A | A |
| GET /customer/orders | L | A | F | F |
| GET/POST /staff/products | L | F | A | A |
| GET/POST /staff/stock, /staff/orders | L | F | A | A |
| GET /staff/invoice | L | F | A | A |
| GET/POST /admin/categories, /admin/users | L | F | F | A |
| GET /admin/report | L | F | F | A |

SecurityFilter chạy trước AuthFilter. Khi kiểm quyền bằng POST, chuẩn bị token đúng phiên để đi tới nhánh kiểm quyền. POST thiếu hoặc sai CSRF trả 403 trước các kết quả L/F trong bảng.

A chỉ xác nhận được qua filter. Các ca nghiệp vụ tiếp tục kiểm tra validation, tài nguyên tồn tại, chủ sở hữu và phản hồi của DAO. Với `/dashboard`, customer được chuyển tới `/customer/orders`, staff/admin vào dashboard quản lý.

Các route công khai `/login`, `/register`, `/robots.txt`, `/sitemap.xml` và tài nguyên tĩnh được kiểm tra riêng theo chức năng.

## 5. Môi trường, dữ liệu và cách ly

- Java 17 trở lên, Maven, Jetty 12 với H2 demo. Môi trường deploy thay thế dùng Tomcat 10.1 và SQL Server test có schema GearAstral.
- Base URL Jetty là `http://localhost:8080`. Với cấu hình Tomcat hiện tại dùng `http://localhost:8080/GearAstral-SOF309`. Test nhận base URL qua cấu hình phù hợp với cách deploy.
- Chrome và ChromeDriver tương thích. Khi triển khai CI cho UI, có thể chạy Chrome headless với server được khởi động trước.
- Tài khoản customer/staff/admin được seed trên DB test hoặc trả về từ DAO giả lập. Tên, email, tên sản phẩm có hậu tố runId khi cần tạo dữ liệu thật.
- Mật khẩu tài khoản môi trường lấy từ biến môi trường. Dữ liệu minh họa trong unit test là dữ liệu tổng hợp.
- Unit Test tạo mock mới trước mỗi ca. Sau ca đăng nhập, xóa trạng thái LoginAttemptGuard theo IP và username đang dùng.
- Các ca tham số được reset riêng để tránh lỗi trùng dữ liệu che mất kết quả validation.
- Ca tích hợp chụp dữ liệu đơn, chi tiết đơn và tồn trước/sau. Ca lỗi ghi nhận cả trạng thái giỏ và rollback.
- Ca AI dùng provider giả lập hoặc cấu hình test để kiểm soát phản hồi và timeout.

| Fixture | Dữ liệu và điều kiện |
| --- | --- |
| REG | Username mới, password đủ nhóm, họ tên GearAstral QA, email duy nhất, số điện thoại Việt Nam hợp lệ |
| AUTH | Từng tài khoản customer, staff, admin đang hoạt động, thêm tài khoản ngừng hoạt động để kiểm tra âm |
| PROD | Danh mục tồn tại, sản phẩm SSD thử nghiệm, giá 120000, tồn 5, đang bán |
| PROD-INACTIVE | Sản phẩm ngừng bán có ID riêng |
| ID-MISSING | ID dương đã xác nhận không tồn tại trong DB test hoặc mock |
| CART | Phiên và token riêng, giỏ chứa sản phẩm PROD với lượng kiểm soát được |
| ORDER | Hai customer A/B, đơn của A, đơn mới cho từng lượt thay đổi trạng thái |
| STOCK | Sản phẩm tồn 5, tài khoản staff, phiếu nhập riêng từng lượt |
| ADMIN | Tài khoản admin và dữ liệu danh mục/tài khoản được phép sửa trong môi trường test |

Các fixture trên là dữ liệu cần chuẩn bị. ID, tài khoản và email được xác nhận lại trước lượt chạy.

## 6. Điều kiện vào, ra, dừng và tiếp tục

**Điều kiện vào**

Đặc tả và expected được rà soát, project build được, đúng commit, môi trường web/DB sẵn sàng, fixture có đủ dữ liệu và role. Ca POST có token hợp lệ trừ khi đang kiểm tra CSRF. Ca AI có provider test phù hợp.

**Điều kiện ra**

- Hai class Unit Test GearAstral chạy đủ 40 ca, 0 failure, 0 error, 0 skipped.
- Các ca P0 của đợt nghiệm thu được chạy đạt. Ca chưa chạy hoặc Blocked có lý do, điều kiện còn thiếu và kế hoạch xử lý.
- Ca tích hợp xác nhận cả phản hồi và dữ liệu sau thao tác.
- Mỗi dataset EP/BVA có kết quả riêng, lỗi có liên kết tới ca và bằng chứng.
- Không còn lỗi nghiêm trọng trong phạm vi nghiệm thu gây sai quyền, sai đơn, sai tồn hoặc mất dữ liệu.

**Điều kiện dừng và tiếp tục**

Dừng đợt chạy khi môi trường, fixture hoặc kết nối DB không đúng khiến kết quả không đáng tin cậy. Lưu bằng chứng, khôi phục điều kiện cần thiết và chạy lại các ca ảnh hưởng trước khi tiếp tục.

**Blocked và N/A**

Thiếu schema, tài khoản, provider hoặc môi trường gây lỗi có kiểm soát được ghi Blocked. Thao tác UI không thể thực hiện do ràng buộc trình duyệt được giải thích ở kết quả UI và liên kết ca Controller tương ứng. N/A cần có lý do gắn với phạm vi đã duyệt.

## 7. Ưu tiên, quản lý lỗi và bằng chứng

- **P0** dành cho xác thực, phân quyền, CSRF, đơn hàng, giá, tồn kho và quyền sở hữu.
- **P1** dành cho các biên bổ sung, CRUD hỗ trợ, tìm kiếm, báo cáo và hiển thị UI.
- **Critical** là truy cập trái quyền, sai dữ liệu đơn/kho nghiêm trọng hoặc mất dữ liệu. **Major** là luồng chính hỏng. **Minor** là lỗi hiển thị hoặc luồng phụ.
- Ghi ID ca và dataset, commit, môi trường, tiền điều kiện, bước, expected, actual, HTTP/redirect, dữ liệu trước/sau và ảnh UI nếu có.
- Mỗi lỗi được ghi vào [04_debug_logs_ai.md](ai-logs/04_debug_logs_ai.md) hoặc issue tương ứng, kèm kết quả retest.
- Trạng thái gồm Chưa chạy, Pass, Fail, Blocked, N/A. Ca thiết kế mới bắt đầu ở Chưa chạy. Kết quả cũ phải gắn với đúng lần chạy và tầng đã kiểm tra.
- Khi hành vi hiện tại khác đề xuất nghiệp vụ, ghi rõ hành vi từ mã nguồn và nội dung cần xác nhận.

**Bằng chứng tự động**

Báo cáo JUnit nằm tại `target/surefire-reports/`. Workflow `.github/workflows/test.yml` chạy `mvn clean test` trên Temurin 17 và lưu artifact `test-reports`.

```powershell
mvn test "-Dtest=AuthControllerTest,ProductControllerTest"
mvn test "-Dtest=*ControllerTest"
mvn clean verify
```

Lệnh đầu chọn 40 ca GearAstral. Lệnh wildcard chạy thêm 2 ca SampleControllerTest. Lệnh `mvn test` chạy 42 ca JUnit; profile `ui` chạy các ca Selenium.

Lượt build đã xác nhận ngày 18/11/2026 có 42 ca chạy đạt, 0 failure, 0 error và 1 skipped. Đây là kết quả của bộ test hiện có. Các ca UI và tích hợp mở rộng được ghi kết quả sau khi thực thi.

## 8. Lộ trình thực hiện

1. Rà soát đặc tả, chuẩn bị DB test, fixture, role, base URL và hợp đồng mock.
2. Chạy bộ 40 ca Controller hiện có, bổ sung các phân vùng và nhánh lỗi theo Test Cases.
3. Triển khai và chạy Filter Test cho CSRF, session và ma trận role.
4. Tích hợp Page Object cho Login, Register, Product với locator JSP GearAstral, sau đó chạy smoke UI.
5. Chạy giỏ, checkout COD, quyền sở hữu, nhập kho và cập nhật trạng thái trên DB test. Đối chiếu dữ liệu và rollback.
6. Ghi lỗi, sửa, retest và regression. Cập nhật actual/evidence cho từng dataset.
7. Duy trì CI cho unit test. Khi chạy UI/tích hợp trên CI, chuẩn bị server, DB và fixture trước job kiểm thử.

<a name="6-truy-vết-test-case-với-code"></a>

## 9. Truy vết

| Đặc tả | Nhóm ca trong TEST_CASES.md | Bộ kiểm thử hoặc bước thực hiện |
| --- | --- | --- |
| §2, §3, §5 – Quyền, route, CSRF | PERM-01–04, ROLE-ADMIN-01 | Ca HTTP hiện có và Filter Test cần bổ sung |
| §4.A – Đăng ký | REG-01–13 | AuthControllerTest, các ca HTTP và UI-REGISTER-01/02 |
| §4.A – Đăng nhập, phiên | LOGIN-01–07 | AuthControllerTest, UI-LOGIN-01/02 |
| §4.C – Sản phẩm | PROD-01–12, PROD-READ-01 | ProductControllerTest, UI-PROD-01 |
| §4.D – Danh mục công khai | SHOP-01–03 | HTTP và UI tìm kiếm, lọc, phân trang |
| §4.D – Giỏ | CART-01–05 | HTTP và tích hợp theo session |
| §4.B, §4.D – Checkout | TC_PAY_01/02, CHECKOUT-01–03 | Tích hợp đơn COD, tồn kho và dữ liệu giao hàng |
| §4.D – Chủ sở hữu đơn | OWNER-01 | Tích hợp hai customer và hai phiên riêng |
| §4.E – Nhập kho | STOCK-01 | Tích hợp phiếu nhập và tồn trước/sau |
| §4.B, §4.E – Thanh toán và trạng thái | TC_PAY_03/04, ORDER-STATUS-01 | HTTP và tích hợp cập nhật đơn |
| §4.E – Danh mục quản trị | CAT-01 | HTTP, role admin, validation và kết quả lưu |
| §3 – Tài khoản admin, báo cáo, hóa đơn, PC Builder, AI | Bổ sung ID chi tiết khi triển khai nhóm này | Hoàn thiện fixture và expected trước thực thi |

Mỗi ID thiết kế có thể gồm nhiều dataset. Mỗi lượt phải có dữ liệu và expected cụ thể. Bảng truy vết được cập nhật cùng bộ Test Cases khi mở rộng tự động hóa.
