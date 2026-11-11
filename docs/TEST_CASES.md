# BỘ KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES)

### Dự án: GearAstral

> **Sinh viên:** Vũ Huỳnh Phúc Khang · TS02543 · SD21304<br>
> **Tài liệu liên quan:** [Đặc tả nghiệp vụ](SPECIFICATIONS.md) và [Kế hoạch kiểm thử](TEST_PLAN.md).

**Cách đọc bảng.** P0 là luồng nghiệm thu, P1 là các phân vùng và biên bổ sung. HTTP là kiểm tra trực tiếp Servlet. UI là thao tác trong trình duyệt. **PASS (HTTP)** áp dụng cho luồng HTTP đã xác nhận. **Một phần** ghi rõ dữ liệu hoặc bước đã xác nhận. Các ca UI và đợt mở rộng thanh toán đang chờ chạy.

Dùng dữ liệu giả lập. Tạo tên đăng nhập, email hoặc tên sản phẩm riêng cho mỗi bộ dữ liệu thành công. Với POST hợp lệ, lấy token CSRF qua GET và giữ cookie cùng phiên. Dùng HTTP khi cần đưa dữ liệu vượt giới hạn nhập của trình duyệt.

---

## 1. MODULE ĐẶT HÀNG & THANH TOÁN (PAYMENT - BÁM SÁT LAB 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **TC_PAY_01** | Tạo đơn thanh toán khi nhận hàng | Positive | Giỏ có sản phẩm đang bán, còn tồn. | 1. Mở `/checkout`.<br>2. Nhập thông tin nhận hàng và đặt hàng.<br>3. Kiểm tra đơn mới trong danh sách nhân viên. | Số lượng `2`, giá hiện tại `120000`, thông tin giao hàng hợp lệ. | Hiện mã đơn. Đơn dùng `cod`, tổng tiền `240000`, trạng thái `pending`. Giỏ trống. | Chưa chạy |
| **TC_PAY_02** | Đặt hàng khi giỏ trống | Negative | Phiên có giỏ trống và token hợp lệ. | Gửi `POST /checkout`. | Giỏ không có sản phẩm. | 302 đến `/cart?error=empty`. Không tạo đơn. | Chưa chạy |
| **TC_PAY_03** | Cập nhật trạng thái đã thanh toán | Positive | Có đơn thử nghiệm và phiên nhân viên. | 1. Mở `/staff/orders`.<br>2. Chọn Đã thanh toán và lưu.<br>3. Tải lại danh sách. | `action=payment`, ID đơn hiện có, `paymentStatus=paid` | 302 đến `/staff/orders?success=updated`. Trạng thái đã lưu là `paid`. | Chưa chạy |
| **TC_PAY_04** | Từ chối trạng thái thanh toán ngoài danh sách | Negative | Có đơn thử nghiệm và phiên nhân viên. | 1. Gửi POST cập nhật trạng thái qua HTTP.<br>2. Đọc lại trạng thái đơn. | `action=payment`, ID đơn hiện có, `paymentStatus=unknown` | 302 đến `/staff/orders?error=payment`. Trạng thái giữ nguyên. | Chưa chạy |

---

## 2. MODULE XÁC THỰC (AUTHENTICATION - SLIDE 1)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **REG-01** | Đăng ký thành công | Positive<br>P0 · HTTP/UI | Tên đăng nhập và email chưa tồn tại. | 1. Mở `/register`, điền và gửi form.<br>2. Đăng nhập bằng tài khoản mới và mở dashboard. | `gearqa01` / `GearTest123!` / Nguyễn An / `090 000 0000` / `qa01@example.test` | 302 đến `/login?success=registered`. Tài khoản mới vào được `/customer/orders`. | **PASS (HTTP)** |
| **REG-02** | Biên dưới tên | Boundary<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | POST từng tên riêng với dữ liệu hợp lệ. | 3, 4, 5 ký tự ASCII | 3 → 400. 4 và 5 → tạo thành công. | **Một phần** (HTTP, 3 ký tự) |
| **REG-03** | Biên trên tên | Boundary<br>P1 · HTTP | Tên đăng nhập chưa tồn tại. | POST từng tên riêng. | 31, 32, 33 ký tự ASCII | 31 và 32 → tạo. 33 → 400 và giữ nguyên dữ liệu. | **Một phần** (HTTP, 33 ký tự) |
| **REG-04** | Ký tự tên | Positive/Negative<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi mỗi giá trị một lần. | qa.user, qa_user, qa-user, qa@user, tên có dấu | Dấu chấm, gạch dưới và gạch ngang hợp lệ. Dấu @ và chữ có dấu bị từ chối. | Chưa chạy |
| **REG-05** | Trùng tên không phân biệt hoa thường | Negative<br>P0 · HTTP | Đã tạo tài khoản từ REG-01. | Đổi mật khẩu rồi thử đăng ký lại bằng tên khác hoa thường. | GEARQA01 sau REG-01 | 400. Cặp thông tin gốc vẫn đăng nhập được. | **Một phần** (HTTP, từ chối trùng tên) |
| **REG-06** | Biên dưới mật khẩu | Boundary<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi mật khẩu và xác nhận giống nhau. | 7, 8, 9 ký tự đủ nhóm | 7 → 400. 8 và 9 → tạo tài khoản. | **Một phần** (HTTP, 7 ký tự) |
| **REG-07** | Biên bcrypt | Boundary<br>P1 · HTTP | Tên đăng nhập chưa tồn tại. | Dùng tên mới cho từng chuỗi và gửi hai trường mật khẩu giống nhau. | Ab1 + 68, 69, 70 ký tự a. Thêm Ab1 + 35 ký tự é. | 71 và 72 byte → tạo. 73 byte ASCII hoặc 73 byte đa byte → 400. | **Một phần** (HTTP, 73 byte đa byte) |
| **REG-08** | Thiếu nhóm mật khẩu hoặc xác nhận lệch | Negative<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi riêng từng bộ. | gear1234, GEAR1234, GearOnly, GearTest123! xác nhận khác | 400 và hiện lỗi độ mạnh hoặc xác nhận tương ứng. | **Một phần** (HTTP, xác nhận lệch) |
| **REG-09** | Biên họ tên | Boundary<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi các giá trị với tên mới. Dùng HTTP để vượt giới hạn UI. | 1, 2, 3, 99, 100, 101 ký tự | 1 → 400. 2 đến 100 → giữ nguyên. 101 → lưu 100 ký tự đầu. | Chưa chạy |
| **REG-10** | Email tùy chọn và định dạng | Positive/Negative<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi từng email với tên mới. | Trống, qa@example.test, qa@domain | Trống và email đúng → tạo. Tên miền thiếu hậu tố → 400. | **Một phần** (HTTP, email thiếu hậu tố) |
| **REG-11** | Trùng email không phân biệt hoa thường | Negative<br>P0 · HTTP | Đã tạo tài khoản có email từ REG-01. | Gửi tên đăng nhập khác với email trùng. | QA01@example.test sau REG-01 | 400 và thông báo email đã được sử dụng. | **Một phần** (HTTP, trùng đúng email gốc) |
| **REG-12** | Biên điện thoại | Boundary<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi từng độ dài. Thử thêm trường hợp có dấu cách. | Tiền tố 0 hoặc +84 với 8, 9, 10, 11 chữ số theo sau | 8 và 11 → 400. 9 và 10 hợp lệ. Dấu cách được bỏ trước khi kiểm tra. | **Một phần** (HTTP, số có dấu cách và số sai định dạng) |
| **REG-13** | Địa chỉ tùy chọn | Boundary<br>P1 · HTTP/UI | Tên đăng nhập chưa tồn tại. | Gửi từng giá trị với dữ liệu còn lại hợp lệ. | Trống, 199, 200, 201 ký tự | Tạo được cả bốn. 201 ký tự được cắt còn 200. | Chưa chạy |
| **LOGIN-01** | Khách hàng đăng nhập đúng | Positive<br>P0 · HTTP/UI | Có tài khoản khách hàng đang hoạt động. | Gửi form và theo chuyển hướng từ dashboard. | Tài khoản REG-01 | 302 đến /dashboard rồi /customer/orders. | **PASS (HTTP)** |
| **LOGIN-02** | Sai mật khẩu | Negative<br>P0 · HTTP/UI | Có tài khoản khách hàng đang hoạt động. | Gửi form và mở lại trang được chuyển đến. | Tài khoản REG-01 với mật khẩu sai | 302 về /login. Hiện ĐĂNG NHẬP THẤT BẠI. | **PASS (HTTP)** |
| **LOGIN-03** | Tên lạ hoặc tài khoản bị tắt | Negative<br>P1 · HTTP | Chuẩn bị tài khoản bị tắt và một tên chưa đăng ký. | Thử mỗi tài khoản với mật khẩu hợp lệ về định dạng. | Tên chưa tạo và tài khoản đã ngừng hoạt động | 302 về /login với cùng thông báo thất bại. | **Một phần** (HTTP, tên chưa đăng ký) |
| **LOGIN-04** | Thiếu trường hoặc sai định dạng | Negative<br>P1 · HTTP/UI | Phiên khách, đã mở form đăng nhập. | Gửi riêng từng bộ qua HTTP và kiểm tra form. | Tên trống, mật khẩu trống, tên dài 33 | Controller chuyển đến /login và báo thông tin không hợp lệ. | Chưa chạy |
| **LOGIN-05** | Ngưỡng khóa tạm thời | Boundary<br>P1 · HTTP | Phiên riêng và tên đăng nhập chưa có lượt thử. | Gửi sáu yêu cầu sai liên tiếp và đọc header. | Tên chưa tạo, sáu lần thử từ cùng IP | Lần 4 và 5 chưa có Retry-After. Lần 6 có Retry-After và thông báo tạm khóa. | **PASS (HTTP)** |
| **LOGIN-06** | Nhân viên đăng nhập và đăng xuất | Positive<br>P0 · HTTP/UI | H2 demo có tài khoản nhân viên. | 1. Đăng nhập và mở `/staff/products`.<br>2. Gửi form đăng xuất.<br>3. Mở lại trang nhân viên. | `demostaff` và mật khẩu demo | Mở danh sách được trước khi đăng xuất. Sau đó quay về /login?error=session. | **PASS (HTTP)** |
| **LOGIN-07** | Đổi mã phiên | Positive<br>P1 · HTTP | Phiên khách có cookie, có tài khoản hợp lệ. | Gửi POST đăng nhập đúng và so sánh cookie mới. | Cookie phiên khách trước khi đăng nhập | Mã phiên được đổi khi đăng nhập thành công. | Chưa chạy |
| **PERM-01** | Khách truy cập khu vực nhân viên | Negative<br>P0 · HTTP | Phiên khách, chưa đăng nhập. | GET danh sách và POST thêm sản phẩm. | Phiên khách có token hợp lệ | Cả hai chuyển đến /login?error=session. Không tạo sản phẩm. | **Một phần** (HTTP, xác nhận chuyển hướng GET và POST) |
| **PERM-02** | Khách hàng thử quản lý sản phẩm | Negative<br>P0 · HTTP | Đã đăng nhập với vai trò khách hàng. | GET danh sách và POST thêm. | Phiên khách hàng đã đăng nhập | Cả hai chuyển đến /dashboard?error=forbidden. Không tạo sản phẩm. | **Một phần** (HTTP, xác nhận chuyển hướng GET và POST) |
| **PERM-03** | CSRF thiếu hoặc sai | Negative<br>P0 · HTTP | Form có token hợp lệ. Chuẩn bị thêm một phiên độc lập. | 1. Gửi POST đăng ký thiếu token.<br>2. Gửi POST cập nhật sản phẩm với token lấy từ phiên khác. | Token trống, token khác phiên | 403. Không thay đổi dữ liệu. | **Một phần** (HTTP, POST đăng ký thiếu token) |
| **PERM-04** | Nhân viên mở danh sách | Positive<br>P0 · HTTP/UI | Đã đăng nhập bằng tài khoản nhân viên. | Mở trang /staff/products. | Phiên demostaff | 200, có bảng sản phẩm và nút Thêm sản phẩm. | **Một phần** (HTTP, xác nhận bảng có dữ liệu) |

---

## 3. MODULE QUẢN LÝ SẢN PHẨM (PRODUCT CRUD)

| Test Case ID | Tên ca kiểm thử | Loại test | Tiền điều kiện | Các bước thực hiện | Dữ liệu đầu vào (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :---: | :--- | :--- | :--- | :--- | :---: |
| **PROD-01** | Thêm và đọc sản phẩm | Positive<br>P0 · HTTP/UI | Phiên nhân viên, danh mục SSD tồn tại. | 1. Lưu sản phẩm mới.<br>2. Tìm theo tên trên bảng nhân viên.<br>3. Mở chi tiết từ phiên khách. | SSD, Gear QA SSD, giá `120000`, tồn `5` | 302 với success=add. Cùng sản phẩm xuất hiện trong bảng và chi tiết. | **PASS (HTTP)** |
| **PROD-02** | Sửa sản phẩm | Positive<br>P0 · HTTP/UI | Đã tạo sản phẩm từ PROD-01. | 1. Sửa sản phẩm và lưu.<br>2. Mở lại chi tiết và danh sách nhân viên. | Tên mới, giá `0`, gửi thêm `stockQty=99999` qua HTTP | 302 với success=update. Tên và giá đổi, tồn kho giữ 5. | **PASS (HTTP)** |
| **PROD-03** | Ngừng bán sản phẩm | Positive<br>P0 · HTTP/UI | Phiên nhân viên, sản phẩm đang bán. | Sửa, chọn Ngừng bán, tải lại danh mục và URL chi tiết. | Sản phẩm đang bán | Bảng nhân viên vẫn có bản ghi. Danh mục ẩn sản phẩm và chi tiết trả 404. | **Một phần** (HTTP, kiểm tra bảng nhân viên và URL chi tiết) |
| **PROD-04** | Mở bán lại | Positive<br>P0 · HTTP/UI | Sản phẩm từ PROD-03 đang ngừng bán. | Đổi trạng thái sang Đang bán rồi mở chi tiết. | Sản phẩm từ PROD-03 | 302 với success=update. Chi tiết công khai trả 200. | **PASS (HTTP)** |
| **PROD-05** | Biên dưới tên sản phẩm | Boundary<br>P1 · HTTP/UI | Phiên nhân viên, danh mục hợp lệ. | Tạo từng sản phẩm với trường còn lại hợp lệ. | 1, 2, 3 ký tự và chỉ khoảng trắng | 1 và toàn khoảng trắng → lỗi. 2 và 3 → tạo được. | **Một phần** (HTTP, tên 1 ký tự) |
| **PROD-06** | Biên trên tên sản phẩm | Boundary<br>P1 · HTTP | Phiên nhân viên, danh mục hợp lệ. | Tạo từng sản phẩm và đọc lại tên đã lưu. | 199, 200, 201 ký tự | 199 và 200 giữ nguyên. 201 được cắt còn 200. | Chưa chạy |
| **PROD-07** | Biên giá dưới và trên | Boundary<br>P1 · HTTP | Phiên nhân viên, danh mục hợp lệ. | Tạo riêng từng sản phẩm và đọc lại giá. | -1, 0, 1, 999999999, 1000000000, 1000000001 | -1 và 1000000001 → lỗi. Các giá trong khoảng được lưu. | **Một phần** (HTTP, giá dưới 0 và vượt trần khi thêm) |
| **PROD-08** | Biên tồn kho | Boundary<br>P1 · HTTP | Phiên nhân viên, danh mục hợp lệ. | Thêm riêng từng sản phẩm. | -1, 0, 1, 99999, 100000, 100001 | -1 và 100001 → lỗi. Các giá trị nguyên trong khoảng được lưu. | **Một phần** (HTTP, tồn kho 100.001) |
| **PROD-09** | Dữ liệu giá hoặc tồn kho sai kiểu | Negative<br>P1 · HTTP | Phiên nhân viên, danh mục hợp lệ. | POST từng bộ với các trường còn lại hợp lệ. | Giá abc, tồn kho 1.5, tồn kho trống | 302 với error=invalid. Không thêm bản ghi. | Chưa chạy |
| **PROD-10** | Danh mục không hợp lệ | Negative<br>P1 · HTTP | Phiên nhân viên, có ID chưa tồn tại để thử. | Thử thêm với mỗi danh mục. | catId=0, ID dương chưa tồn tại | 302 với error=invalid. Không thêm sản phẩm. | **Một phần** (HTTP, ID 0) |
| **PROD-11** | ID sai khi sửa hoặc đọc | Negative<br>P1 · HTTP | Phiên nhân viên, có ID chưa tồn tại để thử. | POST cập nhật rồi mở chi tiết công khai. | ID 0, chữ hoặc chưa tồn tại | Cập nhật chuyển đến error=invalid. Chi tiết trả 404. | Chưa chạy |
| **PROD-12** | Hủy sửa sản phẩm | Positive<br>P1 · UI | Đã mở danh sách sản phẩm trong trình duyệt. | Mở cửa sổ sửa, thay đổi tên rồi bấm Hủy. | Sản phẩm đang bán | Cửa sổ đóng. Bảng và chi tiết giữ tên gốc. | Chưa chạy |

---

## 4. KẾT QUẢ CHẠY NGÀY 07/11/2026

Lượt smoke test GearAstral đạt **53/53 kiểm tra HTTP** tại `localhost:8080` bằng JDK 17, Jetty 12 và H2 demo. Đây là số lượt xác nhận mã HTTP, chuyển hướng, nội dung JSP và dữ liệu sau thao tác.

| Nhóm | Kết quả đã quan sát |
| :--- | :--- |
| Web | Trang gốc chuyển đến danh mục. Danh mục và chi tiết hiển thị sản phẩm có sẵn. |
| Xác thực | Tạo tài khoản, đăng nhập và đăng xuất thành công. Từ chối dữ liệu sai, tên hoặc email trùng. Lần thử thứ 6 sau 5 lần sai có `Retry-After`. |
| Phân quyền | Phiên khách và khách hàng bị chuyển hướng khi truy cập khu vực nhân viên. POST đăng ký thiếu token CSRF trả 403. |
| Sản phẩm | Thêm, đọc, sửa, ngừng bán và mở lại. Giữ tồn kho khi sửa. Từ chối dữ liệu sai ở các biên đã ghi trong bảng. |

Lượt build Tuần 1 chạy đạt hai ca JUnit mẫu. Ca Selenium mẫu đang tắt.
