# 01 – NHẬT KÝ AI THIẾT KẾ KIỂM THỬ

## 1. Thông tin phiên làm việc

- **Ngày:** 18/11/2026.
- **Dự án:** GearAstral – Hệ thống bán linh kiện máy tính và quản lý cửa hàng.
- **Sinh viên:** Vũ Huỳnh Phúc Khang – TS02543 – SD21304.
- **Vai trò yêu cầu AI:** QA Lead.
- **Đầu vào:** [SPECIFICATIONS.md](../SPECIFICATIONS.md) hiện tại trong repository; đối chiếu Servlet, Filter, Service, DAO, JSP và `InputValidator` khi cần xác định validation hoặc phản hồi.
- **Đầu ra:** Nội dung thiết kế trong [TEST_PLAN.md](../TEST_PLAN.md) và [TEST_CASES.md](../TEST_CASES.md); nhật ký này ghi lại cơ sở thiết kế và những điểm cần rà soát.
- **Phạm vi phiên này:** Cập nhật nhật ký thiết kế theo GearAstral và đối chiếu các tài liệu hiện có. Phân biệt ca được thiết kế, kết quả HTTP ghi nhận trước đó và 40 lượt Unit Test tuần 2 đã có; không ghi Pass cho các ca HTTP/UI chưa thực thi.

## 2. Câu lệnh prompt thiết kế kiểm thử

Prompt thiết kế được chuẩn hóa để sử dụng với đặc tả GearAstral:

> Bạn là QA Lead. Dựa trên nội dung SPECIFICATIONS.md của GearAstral, hãy thiết kế Test Plan và bộ Test Case cho Controller, Service, Filter, kiểm thử tích hợp và UI. Áp dụng Phân vùng tương đương (EP) và Phân tích giá trị biên (BVA). Bao phủ đăng ký, đăng nhập, đăng xuất, session, CSRF, phân quyền customer/staff/admin, quản lý sản phẩm, danh mục công khai, giỏ hàng, đặt hàng COD, quyền sở hữu đơn, nhập kho và cập nhật trạng thái đơn/thanh toán. Đưa quản lý tài khoản, báo cáo, hóa đơn, PC Builder và tư vấn cấu hình vào kế hoạch; chỉ ghi ca chi tiết khi xác định đủ dữ liệu và kết quả mong đợi. Với mỗi ca, ghi ID, tầng kiểm thử, ưu tiên, tiền điều kiện, bước, dữ liệu, expected và trạng thái; khi có nhiều dataset thì ghi kết quả từng lượt riêng. Dùng đúng URL và tên trường từ mã nguồn, phân biệt validation HTML với máy chủ, phân biệt cắt chuỗi/fallback với từ chối dữ liệu. Ghi rõ môi trường, fixture, tiêu chí vào/ra, quản lý lỗi, bằng chứng và truy vết. Không tự chuyển yêu cầu GAP thành hành vi đã triển khai hoặc ghi Pass khi chưa chạy. Lưu tài liệu tương ứng vào docs/TEST_PLAN.md và docs/TEST_CASES.md.

## 3. Cách xử lý và kết quả AI tạo

1. Đọc đặc tả GearAstral, đối chiếu `RegisterServlet`, `LoginServlet`, `ProductServlet`, `AuthFilter`, `SecurityFilter` và các luồng giỏ/đơn/kho để xác định URL, tên trường, HTTP status, forward/redirect và quyền truy cập.
2. Chia tầng Controller/Service/Filter, tích hợp H2 hoặc SQL Server test và UI Selenium. Controller gọi Servlet thật với request/response/session và DAO giả lập; kiểm thử DB và trình duyệt có điều kiện thực thi riêng.
3. Áp dụng EP cho thiếu/rỗng/blank, đúng/sai định dạng, tài khoản trùng, role/quyền sở hữu, sản phẩm đang bán/ngừng bán/hết hàng, trạng thái đơn và token CSRF.
4. Áp dụng BVA cho username 3/4/5/31/32/33 ký tự; mật khẩu 7/8/9 ký tự và 71/72/73 byte UTF-8; tên sản phẩm 1/2/3/199/200/201; giá -1/0/1/999.999.999/1.000.000.000/1.000.000.001; tồn ban đầu -1/0/1/99.999/100.000/100.001; lượng giỏ 1–99 và giới hạn tồn. Bổ sung biên họ tên, địa chỉ, điện thoại, số lượng nhập kho và phân trang theo đặc tả.
5. Đối chiếu Test Plan gồm phạm vi, chiến lược theo tầng, EP/BVA, ma trận role, môi trường/fixture, cách ly dữ liệu, tiêu chí vào/ra/dừng, ưu tiên, quản lý lỗi và truy vết.
6. Đối chiếu các nhóm ca hiện có: TC_PAY, REG, LOGIN, PERM, PROD và các ca mở rộng SHOP, CART, CHECKOUT, OWNER, ROLE-ADMIN, STOCK, ORDER-STATUS, CAT, UI. Các chức năng admin, báo cáo, hóa đơn, PC Builder và AI được đưa vào kế hoạch; bộ ca chi tiết vẫn cần bổ sung ở các phần chưa có ID.
7. Giữ trạng thái theo đúng tầng và dataset: một dòng có nhiều giá trị không đồng nghĩa mọi giá trị đã chạy; Pass Unit không thay cho Pass HTTP/UI. Phiếu ghi kết quả từng lượt có dữ liệu thực tế, expected, actual, môi trường/commit, trạng thái và bằng chứng/mã lỗi.

## 4. Các quyết định cần người thực hiện kiểm thử lưu ý

- Dùng base URL `http://localhost:8080` khi chạy tại context gốc; nếu triển khai Tomcat có context riêng phải ghép context đúng. Đăng ký là `/register`, cửa hàng là `/shop`, quản lý sản phẩm là `/staff/products`.
- GearAstral có ba role customer/staff/admin; admin được dùng khu vực staff. Khách được dùng giỏ/checkout/PC Builder. Khu vực bảo vệ thiếu phiên chuyển `/login?error=session`, sai role chuyển `/dashboard?error=forbidden`; CSRF thiếu/sai trả 403.
- Username đăng ký 4–32 ký tự; mật khẩu cần ít nhất 8 ký tự, có hoa/thường/số, tối đa 72 ký tự và 72 byte UTF-8. Có trường xác nhận `confirm`; không tự thêm yêu cầu ký tự đặc biệt.
- Không biến GAP thành yêu cầu đã có: giá 0 hiện hợp lệ; nhiều chuỗi dài bị cắt; lượng giỏ sai dùng fallback 1; unitCost nhập kho sai hiện thành 0. Kiểm thử đúng hành vi hiện tại, tách đề xuất thay đổi thành yêu cầu cần chốt.
- Phân biệt tên trường: giỏ `/cart` nhận `qty`; PC Builder nhận `quantity`; tạo đơn nhân viên nhận `productId[]` và `quantity[]`. PC Builder chưa áp cùng trần 99 của giỏ/checkout, cần kiểm tra luồng liên quan.
- Checkout công khai dùng COD; đơn tại quầy ghi cod/qr. Ghi nhận phương thức qr hoặc paymentStatus=paid không phải bằng chứng xác nhận giao dịch ngân hàng.
- Tạo đơn phải kiểm tra lại giá/tồn và transaction đơn–chi tiết–trừ kho; hủy/khôi phục cần kiểm tra tồn trước/sau. Unit Test với DAO giả lập không xác nhận transaction DB.
- Ca tích hợp cần schema, fixture và tài khoản đúng role; nếu thiếu điều kiện thì ghi Blocked. Ca UI chưa chạy giữ Chưa chạy; không lấy kết quả HTTP hoặc Unit để thay.
- Fixture là dữ liệu tổng hợp cần seed hoặc dữ liệu được mock, không phải cam kết tài khoản có sẵn. H2 demo tạo staff `demostaff` khi cấu hình hợp lệ; admin/customer cần chuẩn bị riêng. Không đưa mật khẩu môi trường, token CSRF, cookie phiên hoặc secret thật vào bằng chứng.

## 5. Kiểm tra tài liệu và phần sinh viên bổ sung

- AI đối chiếu bố cục nhật ký, liên kết tương đối, URL/tên trường và các biên chính với tài liệu/mã GearAstral. Không sử dụng số lượng ca hoặc kết quả thực thi của dự án khác.
- Test Plan mới và đặc tả mới đã được sửa trong thư mục local. Các ký hiệu §4.A–§4.E trong bảng truy vết cũ được quy đổi ở cuối SPECIFICATIONS.md; cần rà soát lại số mục khi tiếp tục cập nhật bộ Test Case.
- Phạm vi Test Plan rộng hơn bộ ca chi tiết hiện có. Quản lý tài khoản, báo cáo, hóa đơn, PC Builder và AI cần hoàn thiện ID, fixture, bước và expected trước khi nghiệm thu các nhóm này.
- **Sinh viên tự chỉnh sửa:** Chưa có xác nhận về phần tự chỉnh sửa của sinh viên trong phiên cập nhật này; cần tự rà soát nghiệp vụ và ghi nhận phần thực sự đã sửa.
- **Cần thực hiện tiếp:** Duyệt kỳ vọng theo mã hiện tại; xác nhận DB/context; chuẩn bị fixture; bổ sung script/ca còn thiếu; chạy và điền actual/status/evidence/defect theo kết quả thật.

## Tuần 2 — Phạm vi JUnit Y3

- **Đăng ký và đăng nhập:** [AuthControllerTest.java](../../src/test/java/com/poly/sof309/controllers/AuthControllerTest.java) có 20 lượt: 6 biên username, 8 biên mật khẩu/kích thước UTF-8, 2 ca trùng username/email, 1 ca lỗi lưu tài khoản và 3 ca đăng nhập đúng/sai/mật khẩu trống. Kiểm tra mã HTTP, chuyển hướng, thông báo, đổi session ID và dữ liệu truyền xuống DAO.
- **Sản phẩm:** [ProductControllerTest.java](../../src/test/java/com/poly/sof309/controllers/ProductControllerTest.java) có 20 lượt: 18 bộ dữ liệu biên tên/giá/tồn hoặc sai kiểu; 1 ca cập nhật giữ tồn và đổi trạng thái; 1 ca tìm kiếm/chuyển dữ liệu tới JSP.
- **Kỹ thuật:** `@ParameterizedTest` + `@CsvSource` cho dữ liệu biên; `assertThrows(DuplicateAccountException.class, ...)` cho tài khoản trùng tại RegistrationService, đồng thời kiểm tra RegisterServlet trả HTTP 400; `@Timeout(value = 2, unit = TimeUnit.SECONDS)` áp dụng cho ca cập nhật sản phẩm.
- **Cách ly:** Mockito giả lập DAO, request, response, dispatcher và session; không cần khởi động SQL Server hoặc Tomcat cho hai class này. Kết quả mock không chứng minh dữ liệu đã lưu trong DB thật.
- **Kết quả đã kiểm tra ngày 18/11/2026:** Hai class GearAstral chạy 40 lượt, 0 failure, 0 error, 0 skipped; BUILD SUCCESS. Đây là kết quả Unit Test, tách với các ca HTTP/UI thiết kế trong Y2.
- **Lệnh chạy đúng hai class GearAstral:** `mvn test "-Dtest=AuthControllerTest,ProductControllerTest"`. Lệnh `mvn test "-Dtest=*ControllerTest"` sẽ gồm thêm SampleControllerTest từ template.
- **Minh chứng chi tiết:** [02_junit_script_ai.md](02_junit_script_ai.md), [04_debug_logs_ai.md](04_debug_logs_ai.md) và báo cáo Maven Surefire tại `target/surefire-reports` sau khi chạy.
