# Bộ kịch bản kiểm thử GearAstral

**SOF309 · Tuần 1 · Vũ Huỳnh Phúc Khang · TS02543 · SD21304**

Các ca dựa trên [đặc tả](SPECIFICATIONS.md). HTTP là yêu cầu trực tiếp đến Servlet với cookie và mã CSRF phù hợp. UI là thao tác qua trang trong trình duyệt. P0 là luồng nghiệm thu, P1 là các phân vùng và biên bổ sung.

Mỗi bộ dữ liệu thành công nên dùng tên đăng nhập, email hoặc tên sản phẩm riêng. Khi ca chỉ thay đổi một trường, điền các trường còn lại bằng dữ liệu hợp lệ. Sử dụng HTTP để đưa dữ liệu vượt giới hạn nhập của trình duyệt.

| Mã | Ưu tiên | Lớp | Mục tiêu | Dữ liệu | Thực hiện | Kết quả mong đợi |
| --- | --- | --- | --- | --- | --- | --- |
| REG-01 | P0 | HTTP/UI | Đăng ký thành công | gearqa01 / GearTest123! / Nguyễn An / 090 000 0000 / qa01@example.test | Mở form, điền đủ, gửi và đăng nhập bằng tài khoản mới. | 302 đến /login?success=registered. Tài khoản là khách hàng. |
| REG-02 | P1 | HTTP/UI | Biên dưới tên | 3, 4, 5 ký tự ASCII | POST từng tên riêng với dữ liệu hợp lệ. | 3 → 400. 4 và 5 → tạo thành công. |
| REG-03 | P1 | HTTP | Biên trên tên | 31, 32, 33 ký tự ASCII | POST từng tên riêng. | 31 và 32 → tạo. 33 → 400 và giữ nguyên dữ liệu. |
| REG-04 | P1 | HTTP/UI | Ký tự tên | qa.user, qa_user, qa-user, qa@user, tên có dấu | Gửi mỗi giá trị một lần. | Dấu chấm, gạch dưới và gạch ngang hợp lệ. Dấu @ và chữ có dấu bị từ chối. |
| REG-05 | P0 | HTTP | Trùng tên không phân biệt hoa thường | GEARQA01 sau REG-01 | Đổi mật khẩu rồi thử đăng ký lại bằng tên khác hoa thường. | 400. Cặp thông tin gốc vẫn đăng nhập được. |
| REG-06 | P1 | HTTP/UI | Biên dưới mật khẩu | 7, 8, 9 ký tự đủ nhóm | Gửi mật khẩu và xác nhận giống nhau. | 7 → 400. 8 và 9 → tạo tài khoản. |
| REG-07 | P1 | HTTP | Biên bcrypt | Ab1 + 68, 69, 70 ký tự a. Thêm Ab1 + 35 ký tự é. | Dùng tên mới cho từng chuỗi và gửi hai trường mật khẩu giống nhau. | 71 và 72 byte → tạo. 73 byte ASCII hoặc 73 byte đa byte → 400. |
| REG-08 | P1 | HTTP/UI | Thiếu nhóm mật khẩu hoặc xác nhận lệch | gear1234, GEAR1234, GearOnly, GearTest123! xác nhận khác | Gửi riêng từng bộ. | 400 và hiện lỗi độ mạnh hoặc xác nhận tương ứng. |
| REG-09 | P1 | HTTP/UI | Biên họ tên | 1, 2, 3, 99, 100, 101 ký tự | Gửi các giá trị với tên mới. Dùng HTTP để vượt giới hạn UI. | 1 → 400. 2 đến 100 → giữ nguyên. 101 → lưu 100 ký tự đầu. |
| REG-10 | P1 | HTTP/UI | Email tùy chọn và định dạng | Trống, qa@example.test, qa@domain | Gửi từng email với tên mới. | Trống và email đúng → tạo. Tên miền thiếu hậu tố → 400. |
| REG-11 | P0 | HTTP | Trùng email không phân biệt hoa thường | QA01@example.test sau REG-01 | Gửi tên đăng nhập khác với email trùng. | 400 và thông báo email đã được sử dụng. |
| REG-12 | P1 | HTTP/UI | Biên điện thoại | Tiền tố 0 hoặc +84 với 8, 9, 10, 11 chữ số theo sau | Gửi từng độ dài. Thử thêm trường hợp có dấu cách. | 8 và 11 → 400. 9 và 10 hợp lệ. Dấu cách được bỏ trước khi kiểm tra. |
| REG-13 | P1 | HTTP/UI | Địa chỉ tùy chọn | Trống, 199, 200, 201 ký tự | Gửi từng giá trị với dữ liệu còn lại hợp lệ. | Tạo được cả bốn. 201 ký tự được cắt còn 200. |
| LOGIN-01 | P0 | HTTP/UI | Khách hàng đăng nhập đúng | Tài khoản REG-01 | Gửi form và theo chuyển hướng từ dashboard. | 302 đến /dashboard rồi /customer/orders. |
| LOGIN-02 | P0 | HTTP/UI | Sai mật khẩu | Tài khoản REG-01 với mật khẩu sai | Gửi form và mở lại trang được chuyển đến. | 302 về /login. Hiện ĐĂNG NHẬP THẤT BẠI. |
| LOGIN-03 | P1 | HTTP | Tên lạ hoặc tài khoản bị tắt | Tên chưa tạo và tài khoản đã ngừng hoạt động | Thử mỗi tài khoản với mật khẩu hợp lệ về định dạng. | 302 về /login với cùng thông báo thất bại. |
| LOGIN-04 | P1 | HTTP/UI | Thiếu trường hoặc sai định dạng | Tên trống, mật khẩu trống, tên dài 33 | Gửi riêng từng bộ qua HTTP và kiểm tra form. | Controller chuyển đến /login và báo thông tin không hợp lệ. |
| LOGIN-05 | P1 | HTTP | Ngưỡng khóa tạm thời | Tên chưa tạo, sáu lần thử từ cùng IP | Gửi sáu yêu cầu sai liên tiếp và đọc header. | Lần 4 và 5 chưa có Retry-After. Lần 6 có Retry-After và thông báo tạm khóa. |
| LOGIN-06 | P0 | HTTP/UI | Nhân viên đăng nhập và đăng xuất | demostaff với mật khẩu demo | Đăng nhập, mở danh sách sản phẩm, dùng nút đăng xuất, mở lại danh sách. | Mở danh sách được trước khi đăng xuất. Sau đó quay về /login?error=session. |
| LOGIN-07 | P1 | HTTP | Đổi mã phiên | Cookie phiên khách trước khi đăng nhập | Gửi POST đăng nhập đúng và so sánh cookie mới. | Mã phiên được đổi khi đăng nhập thành công. |
| PERM-01 | P0 | HTTP | Khách truy cập khu vực nhân viên | Phiên khách có token hợp lệ | GET danh sách và POST thêm sản phẩm. | Cả hai chuyển đến /login?error=session. Không tạo sản phẩm. |
| PERM-02 | P0 | HTTP | Khách hàng thử quản lý sản phẩm | Phiên khách hàng đã đăng nhập | GET danh sách và POST thêm. | Cả hai chuyển đến /dashboard?error=forbidden. Không tạo sản phẩm. |
| PERM-03 | P0 | HTTP | CSRF thiếu hoặc sai | Token trống, token khác phiên | POST đăng ký hoặc cập nhật sản phẩm với token sai. | 403. Không thay đổi dữ liệu. |
| PERM-04 | P0 | HTTP/UI | Nhân viên mở danh sách | Phiên demostaff | Mở trang /staff/products. | 200, có bảng sản phẩm và nút Thêm sản phẩm. |
| PROD-01 | P0 | HTTP/UI | Thêm và đọc sản phẩm | SSD, Gear QA SSD, giá 120000, tồn 5 | Lưu sản phẩm, tìm theo tên và mở chi tiết công khai. | 302 với success=add. Cùng sản phẩm xuất hiện trong bảng và chi tiết. |
| PROD-02 | P0 | HTTP/UI | Sửa sản phẩm | Sản phẩm vừa tạo, tiêu đề mới, giá 0, thử gửi tồn kho khác | Lưu và mở lại chi tiết và danh sách. | 302 với success=update. Tên và giá đổi, tồn kho giữ 5. |
| PROD-03 | P0 | HTTP/UI | Ngừng bán sản phẩm | Sản phẩm đang bán | Sửa, chọn Ngừng bán, tải lại danh mục và URL chi tiết. | Bảng nhân viên vẫn có bản ghi. Danh mục ẩn sản phẩm và chi tiết trả 404. |
| PROD-04 | P0 | HTTP/UI | Mở bán lại | Sản phẩm từ PROD-03 | Đổi trạng thái sang Đang bán rồi mở chi tiết. | 302 với success=update. Chi tiết công khai trả 200. |
| PROD-05 | P1 | HTTP/UI | Biên dưới tên sản phẩm | 1, 2, 3 ký tự và chỉ khoảng trắng | Tạo từng sản phẩm với trường còn lại hợp lệ. | 1 và toàn khoảng trắng → lỗi. 2 và 3 → tạo được. |
| PROD-06 | P1 | HTTP | Biên trên tên sản phẩm | 199, 200, 201 ký tự | Tạo từng sản phẩm và đọc lại tên đã lưu. | 199 và 200 giữ nguyên. 201 được cắt còn 200. |
| PROD-07 | P1 | HTTP | Biên giá dưới và trên | -1, 0, 1, 999999999, 1000000000, 1000000001 | Tạo riêng từng sản phẩm và đọc lại giá. | -1 và 1000000001 → lỗi. Các giá trong khoảng được lưu. |
| PROD-08 | P1 | HTTP | Biên tồn kho | -1, 0, 1, 99999, 100000, 100001 | Thêm riêng từng sản phẩm. | -1 và 100001 → lỗi. Các giá trị nguyên trong khoảng được lưu. |
| PROD-09 | P1 | HTTP | Dữ liệu giá hoặc tồn kho sai kiểu | Giá abc, tồn kho 1.5, tồn kho trống | POST từng bộ với các trường còn lại hợp lệ. | 302 với error=invalid. Không thêm bản ghi. |
| PROD-10 | P1 | HTTP | Danh mục không hợp lệ | catId=0, ID dương chưa tồn tại | Thử thêm với mỗi danh mục. | 302 với error=invalid. Không thêm sản phẩm. |
| PROD-11 | P1 | HTTP | ID sai khi sửa hoặc đọc | ID 0, chữ hoặc chưa tồn tại | POST cập nhật rồi mở chi tiết công khai. | Cập nhật chuyển đến error=invalid. Chi tiết trả 404. |
| PROD-12 | P1 | UI | Hủy sửa sản phẩm | Sản phẩm đang bán | Mở cửa sổ sửa, thay đổi tên rồi bấm Hủy. | Cửa sổ đóng. Bảng và chi tiết giữ tên gốc. |

## Kết quả chạy ngày 07/11/2026

**53/53 lượt kiểm tra HTTP đạt** trên `localhost:8080` bằng JDK 17, Jetty 12 và H2 demo. Các lượt này kiểm tra mã HTTP, cookie, URL chuyển hướng, thông báo JSP và dữ liệu hiển thị sau thao tác.

| Nhóm | Kết quả đã quan sát |
| --- | --- |
| Web | Trang gốc chuyển đến danh mục. Danh mục và chi tiết có sản phẩm GearAstral. |
| Đăng ký | Tạo và đăng nhập được tài khoản mới. Từ chối tên 3 hoặc 33 ký tự, mật khẩu ngắn hoặc vượt 72 byte, xác nhận lệch, email hoặc điện thoại sai, tên trùng và email trùng. |
| Đăng nhập | Sai mật khẩu có thông báo. Khách hàng và nhân viên đăng nhập được. Lần thử thứ 6 sau 5 lần sai có Retry-After. Sau đăng xuất, trang nhân viên yêu cầu đăng nhập lại. |
| Phân quyền | Phiên khách và khách hàng bị chặn khi đọc hoặc POST ở khu vực nhân viên. POST thiếu mã CSRF trả 403. |
| Sản phẩm | Thêm, đọc, sửa, ngừng bán, mở lại và giữ nguyên tồn kho khi sửa. Từ chối giá âm hoặc vượt trần, tồn kho vượt trần, tên quá ngắn và danh mục 0. Chi tiết sản phẩm ngừng bán trả 404. |

Các thao tác trình duyệt và các bộ dữ liệu biên chưa liệt kê ở trên ở trạng thái chưa chạy. Hai test JUnit mẫu của template chạy đạt khi build, còn test Selenium mẫu đang tắt.
