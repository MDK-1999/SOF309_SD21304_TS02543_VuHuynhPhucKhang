# ĐẶC TẢ NGHIỆP VỤ WEB CÁ NHÂN (SPECIFICATIONS)

## 1. Thông tin dự án

- **Tên đề tài:** GearAstral – Hệ thống bán linh kiện máy tính và quản lý cửa hàng.
- **Sinh viên:** Vũ Huỳnh Phúc Khang – TS02543 – SD21304.
- **Công nghệ:** Java 17, Jakarta Servlet 6.0, JSP 3.1, JSTL 3, JDBC, Microsoft SQL Server; H2 cho chế độ demo; Maven đóng gói WAR, chạy bằng Jetty 12 hoặc triển khai trên Tomcat 10.1; HTML/CSS/JavaScript.
- **URL gốc local:** `http://localhost:8080` khi triển khai tại context gốc; ví dụ `http://localhost:8080/shop`, `http://localhost:8080/login`. Nếu Tomcat triển khai WAR dưới context riêng, thêm context đó trước đường dẫn.
- **Mục đích:** Đáp ứng bước 1.4 của assignment SOF309, cung cấp đặc tả nghiệp vụ GearAstral để xây dựng Test Plan và Test Case.
- **Ngày rà soát:** 18/11/2026.

**Căn cứ và quy ước:** Đặc tả dựa trên Servlet, Filter, Service, DAO, Model, JSP, cấu hình Maven/web và schema demo trong repository. Các bảng mô tả hành vi hiện có; quy tắc chỉ có ở giao diện được ghi riêng. Các đề xuất tại mục 7 chưa phải chức năng đã triển khai. Đây là kết quả rà soát mã, không phải báo cáo các ca kiểm thử đã chạy. Khi có giới hạn chuỗi, cần phân biệt việc máy chủ cắt ngắn dữ liệu với việc từ chối dữ liệu quá dài; schema H2 không thay thế việc xác minh schema SQL Server thực tế.

## 2. Người dùng và phân quyền

| Đối tượng / role | Phạm vi nghiệp vụ | Trang sau đăng nhập |
| --- | --- | --- |
| Chưa đăng nhập | Xem cửa hàng, chi tiết linh kiện, giỏ hàng, đặt hàng COD, PC Builder, tư vấn cấu hình; đăng nhập và đăng ký | Không có trang sau đăng nhập; trang gốc chuyển tới `/shop` |
| `customer` | Chức năng công khai; xem danh sách và chi tiết đơn thuộc tài khoản mình | `/dashboard` chuyển tới `/customer/orders` |
| `staff` | Dashboard, quản lý sản phẩm, nhập kho, lập và cập nhật đơn hàng, xem hóa đơn | `/dashboard` |
| `admin` | Quyền staff; quản lý danh mục, tài khoản và báo cáo | `/dashboard` |

- Admin kế thừa quyền staff qua `User.isStaff()`; các URL `/admin/*` chỉ dành cho admin, `/staff/*` dành cho staff/admin và `/customer/*` dành cho customer.
- Khách được sử dụng giỏ hàng, checkout, PC Builder và tư vấn mà không cần đăng nhập; POST vẫn phải qua kiểm tra CSRF.
- URL được bảo vệ: chưa đăng nhập chuyển HTTP 302 tới `/login?error=session`; đã đăng nhập nhưng sai role chuyển HTTP 302 tới `/dashboard?error=forbidden`.
- Session hết hạn sau 60 phút không hoạt động; cookie session có `HttpOnly`, theo dõi session bằng cookie. Đăng nhập thành công đổi session ID.
- Các phương thức thay đổi dữ liệu yêu cầu header `X-CSRF-Token` hoặc trường `_csrf` khớp token trong session; thiếu/sai trả HTTP 403. Phương thức GET, HEAD, OPTIONS không yêu cầu token.

Nguồn: `src/main/java/com/pcshop/model/User.java`, `filter/AuthFilter.java`, `filter/SecurityFilter.java`, `servlet/DashboardServlet.java` và `src/main/webapp/WEB-INF/web.xml`.

## 3. URL màn hình và điểm xử lý

### 3.1. Tài khoản và bán hàng

| URL tương đối | Phương thức | Màn hình / chức năng | Quyền |
| --- | --- | --- | --- |
| `/`, `/index.jsp` | GET | Chuyển tới cửa hàng `/shop` | Công khai |
| `/login` | GET, POST | Form và xử lý đăng nhập | Công khai |
| `/register` | GET, POST | Đăng ký tài khoản customer | Công khai |
| `/logout` | POST | Hủy session, chuyển về `/login` | Đã đăng nhập |
| `/dashboard` | GET | Dashboard nhân viên/quản trị; customer chuyển tới đơn của mình | Đã đăng nhập |
| `/shop` | GET | Danh mục linh kiện; tìm kiếm, lọc, sắp xếp, phân trang | Công khai |
| `/product?id=...` | GET | Chi tiết linh kiện đang hoạt động | Công khai |
| `/product-image` | GET | Phục vụ ảnh sản phẩm | Công khai |
| `/cart` | GET | Giỏ hàng lưu trong session | Công khai |
| `/cart` | POST action=add/update/remove | Thêm, cập nhật hoặc xóa dòng giỏ hàng | Công khai, cần CSRF |
| `/checkout` | GET, POST | Form giao hàng và tạo đơn COD | Công khai, POST cần CSRF |
| `/cart` | POST action=checkout | Xử lý đặt hàng từ giỏ | Công khai, cần CSRF |
| `/pc-builder` | GET, POST | Chọn linh kiện cấu hình máy, đưa vào giỏ | Công khai, POST cần CSRF |
| `/ai-chat` | POST | Tư vấn linh kiện/cấu hình, trả JSON | Công khai, cần CSRF |
| `/customer/orders` | GET | Danh sách đơn của tài khoản hiện tại | customer |
| `/customer/orders?id=...` | GET | Chi tiết đơn thuộc tài khoản hiện tại | customer |
| `/robots.txt`, `/sitemap.xml` | GET | Thông tin cho công cụ tìm kiếm | Công khai |

### 3.2. Quản trị

| URL tương đối | Phương thức / tham số | Chức năng | Quyền |
| --- | --- | --- | --- |
| `/staff/products` | GET; q khi tìm kiếm | Danh sách và form quản lý sản phẩm | staff, admin |
| `/staff/products` | POST action=add/update | Thêm/sửa sản phẩm, đổi trạng thái hoạt động | staff, admin |
| `/staff/stock` | GET | Danh sách sản phẩm và lịch sử nhập kho gần nhất | staff, admin |
| `/staff/stock` | POST | Lập phiếu nhập và cộng tồn kho | staff, admin |
| `/staff/orders` | GET | Danh sách tối đa 100 đơn, dữ liệu lập đơn | staff, admin |
| `/staff/orders` | POST action=create | Lập đơn bán linh kiện | staff, admin |
| `/staff/orders` | POST action=status | Cập nhật trạng thái đơn hàng | staff, admin |
| `/staff/orders` | POST action=payment | Cập nhật trạng thái thanh toán | staff, admin |
| `/staff/invoice?id=...` | GET | Xem hóa đơn của một đơn hàng | staff, admin |
| `/admin/categories` | GET | Danh sách và form danh mục | admin |
| `/admin/categories` | POST action=add/update | Thêm/sửa danh mục và trạng thái | admin |
| `/admin/users` | GET | Danh sách và form tài khoản | admin |
| `/admin/users` | POST action=add/edit/toggle | Tạo/sửa, đặt lại mật khẩu hoặc bật/tắt tài khoản | admin |
| `/admin/report` | GET | Doanh thu, số đơn, số khách, biểu đồ tuần và 30 đơn gần nhất | admin |

**Đối chiếu đề bài:** URL đăng ký thực tế là `/register`; danh mục công khai là `/shop` và trang quản lý sản phẩm là `/staff/products`, không có ánh xạ `/products`. JSP nằm trong `src/main/webapp/WEB-INF/views` và được mở qua Servlet. Các URL tương đối cần ghép với URL gốc tại mục 1.

## 4. Trường dữ liệu và validation

### 4.1. Đăng nhập – `/login`

| Trường | Ràng buộc |
| --- | --- |
| username | Bắt buộc, 4–32 ký tự thuộc `[A-Za-z0-9._-]`. Máy chủ chuẩn hóa với giới hạn 33 ký tự trước khi kiểm tra để phát hiện tên vượt 32 ký tự. |
| password | Bắt buộc, không blank; không vượt 72 ký tự và 72 byte UTF-8. Đăng nhập kiểm tra mật khẩu với hash bcrypt, không áp lại yêu cầu hoa/thường/số của đăng ký. |
| _csrf | Token hợp lệ của session, hoặc dùng header `X-CSRF-Token`. |

- Thiếu/sai định dạng: chuyển HTTP 302 về `/login` và hiển thị thông báo thông tin không hợp lệ. Sai thông tin hoặc tài khoản ngừng hoạt động hiển thị đăng nhập thất bại.
- Sau 5 lần thất bại thuộc bộ đếm IP và username, lần tiếp theo bị tạm khóa trong 5 phút; phản hồi có `Retry-After`. Không coi đây là lỗi mật khẩu thông thường.
- Thành công xóa bộ đếm thất bại, đổi session ID, lưu `loginUser` và chuyển tới `/dashboard`; customer tiếp tục chuyển tới `/customer/orders`. GET login khi đã đăng nhập cũng chuyển tới dashboard.

### 4.2. Đăng ký tài khoản – `/register`

| Trường | Bắt buộc | Validation thực tế |
| --- | --- | --- |
| username | Có | Sau chuẩn hóa phải 4–32 ký tự, chỉ chữ Latin, chữ số, dấu chấm, gạch dưới hoặc gạch ngang; kiểm tra trùng không phân biệt hoa/thường. |
| password | Có | Chuỗi gốc ít nhất 8 ký tự, tối đa 72 ký tự và 72 byte UTF-8; có chữ hoa, chữ thường và chữ số; không bắt buộc ký tự đặc biệt. |
| confirm | Có | Phải bằng chính xác password; không trim hoặc chuẩn hóa mật khẩu trước so sánh. |
| fullName | Có | Chuẩn hóa, cắt tối đa 100 ký tự, sau đó phải dài ít nhất 2 ký tự. |
| email | Không | Chuẩn hóa, cắt tối đa 100 ký tự. Nếu có phải đúng định dạng email với hậu tố tên miền 2–63 chữ cái; kiểm tra trùng không phân biệt hoa/thường. |
| phone | Có | Bỏ dấu cách, chấm, ngoặc và gạch ngang; khớp `^(?:\+84\|0)[0-9]{9,10}$`. |
| address | Không | Chuẩn hóa, cắt tối đa 200 ký tự. |
| _csrf | Có | Token hợp lệ. |

- Form có trường xác nhận `confirm`; mật khẩu được kiểm tra theo byte UTF-8 trước khi băm bcrypt.
- Tài khoản mới luôn có role `customer`; người đăng ký không được tự chọn staff/admin.
- Thành công chuyển HTTP 302 tới `/login?success=registered`; không tự đăng nhập.
- Dữ liệu sai hoặc username/email trùng: HTTP 400, hiển thị lại form với lỗi. `RegistrationService` phát sinh `DuplicateAccountException` khi phát hiện trùng.
- Nếu DAO không lưu được tài khoản: HTTP 409 và hiển thị lại form; không coi việc validation hợp lệ là bằng chứng tạo tài khoản thành công.

### 4.3. Đăng xuất và phiên đăng nhập – `/logout`

| Trường | Ràng buộc |
| --- | --- |
| loginUser | Phải có người dùng trong session để truy cập endpoint được bảo vệ. |
| _csrf / X-CSRF-Token | POST phải có token đúng; thiếu/sai bị SecurityFilter từ chối trước khi hủy session. |
| phương thức | POST thực hiện đăng xuất; GET chuyển về trang gốc, không hủy session. |

Thành công hủy session và chuyển về `/login`. Truy cập lại khu vực bảo vệ cần đăng nhập mới. GearAstral hiện không có endpoint tự đổi mật khẩu riêng; admin có thể đặt lại mật khẩu trong luồng sửa tài khoản tại mục 4.5.

### 4.4. CRUD sản phẩm – `/staff/products`

| Trường | Kiểu / bắt buộc | Ràng buộc hiện tại |
| --- | --- | --- |
| productId | Số nguyên khi sửa | > 0, phải lấy được sản phẩm hiện có. Sai hoặc không tồn tại khiến thao tác thất bại. |
| productName | Chuỗi, có | Chuẩn hóa, cắt tối đa 200 ký tự, dài ít nhất 2 sau chuẩn hóa; chưa kiểm tra tên trùng tại Servlet. |
| catId | Số nguyên, có | > 0; tham chiếu danh mục được kiểm tra thêm bởi khóa ngoại DB. |
| price | BigDecimal, có | Từ 0 đến 1.000.000.000; thiếu, sai định dạng, âm hoặc vượt trần bị từ chối. Không nhận NaN/Infinity. |
| stockQty | Số nguyên khi thêm | 0–100.000; sai định dạng hoặc ngoài khoảng bị từ chối. Khi sửa sản phẩm, máy chủ giữ tồn kho cũ, không lấy stockQty từ form. |
| brand | Chuỗi, không | Chuẩn hóa, cắt tối đa 80 ký tự. |
| model | Chuỗi, không | Chuẩn hóa, cắt tối đa 100 ký tự. |
| description | Chuỗi nhiều dòng, không | Chuẩn hóa, cắt tối đa 2.000 ký tự. |
| specs | Chuỗi nhiều dòng, không | Chuẩn hóa, cắt tối đa 1.000 ký tự. |
| isActive | Trạng thái khi sửa | Chỉ chuỗi `true` được hiểu là hoạt động; sản phẩm thêm mới mặc định hoạt động. |
| action, _csrf | Điều khiển | action=add/update; POST cần CSRF. |

Lưu thành công chuyển HTTP 302 về danh sách với `success=add` hoặc `success=update`; thao tác sai hoặc DAO thất bại chuyển `error=invalid`. Ngừng bán bằng `isActive=false`, không có action xóa cứng trong Servlet này. Giá 0 hiện được chấp nhận; quy tắc giá bắt buộc > 0 cần được chốt riêng tại mục 7.

### 4.5. Quản lý tài khoản – `/admin/users`

| Trường | Ràng buộc |
| --- | --- |
| userId | Số nguyên > 0 khi sửa/bật/tắt; phải tồn tại khi sửa. Không được tự bật/tắt tài khoản đang đăng nhập. |
| username | Khi tạo: chuẩn hóa, cắt tối đa 32 ký tự rồi kiểm tra regex 4–32 và trùng username. Khi sửa: giữ username cũ. |
| password | Tạo mới: chính sách mạnh như mục 4.2. Sửa: null/blank để giữ mật khẩu, có nhập phải đạt chính sách trước khi reset. |
| fullName | Chuẩn hóa, cắt tối đa 100 ký tự, dài ít nhất 2. |
| email | Không bắt buộc, cắt tối đa 100; nếu nhập phải đúng định dạng. Khi tạo có kiểm tra trùng email tại Servlet; khi sửa cần đối chiếu thêm xử lý DAO/DB. |
| phone, address | Phone không bắt buộc; nếu có phải hợp lệ sau chuẩn hóa. Address cắt tối đa 200 ký tự. |
| role | `admin`, `staff` hoặc `customer`; giá trị khác được chuẩn hóa về customer. Khi tự sửa, role hiện tại được giữ lại. |
| action | add/edit/toggle; giá trị khác không thực hiện thay đổi. |

Chỉ admin truy cập được luồng này. Thành công chuyển về danh sách với `success=1`; thất bại `error=invalid`. Khi sửa, cập nhật hồ sơ diễn ra trước bước kiểm tra/reset mật khẩu; vì vậy mật khẩu mới sai có thể làm phản hồi báo lỗi dù hồ sơ đã được lưu, cần kiểm thử và xử lý theo GAP-04.

### 4.6. Quản lý danh mục – `/admin/categories`

Chuỗi được chuẩn hóa bằng `InputValidator`; trường một dòng bỏ khoảng trắng thừa, thay dấu ngoặc nhọn và cắt theo giới hạn trước khi kiểm tra.

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| catId | Khi sửa | Số nguyên > 0. |
| catName | Có | Chuẩn hóa, cắt tối đa 100 ký tự, dài ít nhất 2. |
| description | Không | Chuỗi nhiều dòng, cắt tối đa 500 ký tự. |
| icon | Không ở tầng Servlet | Chuẩn hóa, cắt tối đa 50 ký tự; schema H2 demo giới hạn cột 30, cần đối chiếu GAP-03. |
| isActive | Khi sửa trạng thái | Thêm mới mặc định true; sửa chỉ nhận chuỗi `true` là hoạt động. |
| action, _csrf | Có | action=add/update và token hợp lệ. |

### 4.7. Nhập kho – POST `/staff/stock`

Staff/admin nhập hàng cho sản phẩm hiện có. Lập phiếu nhập và cộng tồn được thực hiện trong một transaction; nếu một bước SQL thất bại thì rollback.

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| productId | Có | Số nguyên > 0, sản phẩm tồn tại. |
| quantity | Có | Số nguyên từ 1 đến 100.000; sai định dạng/ngoài khoảng bị từ chối. |
| unitCost | Giá vốn | BigDecimal từ 0 đến 1.000.000.000; thiếu/sai/ngoài khoảng hiện được thay bằng 0, không tự trả lỗi validation. |
| supplier | Không | Chuẩn hóa, cắt tối đa 150 ký tự. |
| note | Không | Chuỗi nhiều dòng, cắt tối đa 500 ký tự. |
| loginUser, _csrf | Có | Có phiên staff/admin; staff_id lấy từ phiên, không lấy từ form; CSRF hợp lệ. |

Thành công chuyển `/staff/stock?success=imported`; dữ liệu sản phẩm/số lượng không hợp lệ chuyển `error=invalid`; lỗi transaction chuyển `error=database`. GET hiển thị tối đa 30 lượt nhập gần nhất. Giới hạn 100.000 áp dụng cho số lượng mỗi lần nhập, không phải trần tổng tồn sau cộng.

### 4.8. Giỏ hàng, danh mục và tìm kiếm

| Trường / tình huống | Quy tắc |
| --- | --- |
| productId khi thêm/cập nhật | Số nguyên > 0; sản phẩm phải tồn tại, hoạt động và còn hàng; nếu không chuyển `/cart?error=unavailable`. |
| qty (số lượng trong giỏ) | Số nguyên 1–99; thiếu/sai/ngoài khoảng được thay bằng 1. Số lượng thực tế không vượt tồn và tối đa 99 mỗi dòng. |
| Thêm trùng / cập nhật | Thêm trùng cộng số lượng rồi giới hạn; action=update đặt lại số lượng dòng đã có. |
| Xóa dòng | action=remove, xóa dòng tương ứng productId trong session rồi chuyển về giỏ. |
| q, brand, sort | Tham số lọc/tìm/sắp xếp tại `/shop`; chuẩn hóa và cắt lần lượt 100/80/16 ký tự. |
| catId, min, max | Lọc danh mục bằng ID dương; min/max là tiền không âm tới 1.000.000.000, sai thành null. Nếu min > max, máy chủ hoán đổi hai giá trị. |
| page | Số nguyên 1–100.000, sai mặc định 1; mỗi trang 24 sản phẩm, trang vượt tổng số được đưa về trang cuối. |
| id ở trang product | Phải tìm được sản phẩm hoạt động; sai/không tồn tại/ngừng bán trả HTTP 404. |

Giỏ nằm trong session và tách giữa các trình duyệt/phiên. Tồn kho ở giỏ không thay thế việc đọc lại sản phẩm và kiểm tra tồn khi checkout. POST giỏ cần CSRF; sau thao tác thường chuyển HTTP 302 về `/cart`.

### 4.9. Đặt hàng và thanh toán

| Trường | Ràng buộc |
| --- | --- |
| customerName | Checkout: chuẩn hóa, cắt tối đa 100 ký tự và dài ít nhất 2. |
| customerPhone | Checkout bắt buộc; chuẩn hóa và kiểm tra số Việt Nam như mục 4.2. Lập đơn tại quầy cho phép rỗng, nếu nhập phải đúng định dạng. |
| customerEmail | Không bắt buộc, cắt tối đa 100; nếu có phải đúng định dạng. |
| shippingAddr | Checkout: cắt tối đa 200 và dài ít nhất 5. Lập đơn tại quầy chỉ chuẩn hóa/cắt 200, không áp min 5 trong Servlet. |
| notes | Không bắt buộc, nhiều dòng, cắt tối đa 500 ký tự. |
| productId[], quantity[] | Lập đơn tại quầy: hai mảng cùng độ dài, không rỗng; sản phẩm hoạt động; mỗi lượng 1–99 và không vượt tồn. Checkout lấy dòng từ giỏ phía máy chủ. |
| orderType, paymentMethod | Loại đơn là product. Checkout luôn dùng cod; lập đơn tại quầy nhận qr nếu khớp không phân biệt hoa/thường, còn lại dùng cod. |
| orderId, status | Khi action=status: orderId nguyên dương; status thuộc pending/confirmed/processing/shipping/done/cancelled. |
| orderId, paymentStatus | Khi action=payment: orderId nguyên dương; paymentStatus thuộc unpaid/paid/refunded. |

- Giỏ rỗng chuyển `/cart?error=empty`. Với người đã đăng nhập, các trường người nhận bỏ trống có thể lấy thông tin hồ sơ trước khi validation.
- Giá và tổng được tính lại từ sản phẩm trong DB, không tin giá do trình duyệt gửi. Tiền dòng = unitPrice × quantity; tổng = tổng các dòng; discount=0 và finalAmount=totalAmount trong luồng tạo đơn hiện tại.
- `OrderDAO.createWithItems` trừ tồn có điều kiện, tạo đơn và chi tiết trong cùng transaction; rollback khi thiếu hàng hoặc lỗi lưu. Không trừ kho thêm lần nữa chỉ vì cập nhật thanh toán.
- Checkout sai thông tin nhận hàng trả HTTP 400 và hiển thị lại form. Tồn thay đổi hoặc lưu thất bại trả HTTP 409, giữ giỏ. Thành công xóa giỏ và forward màn hình xác nhận với orderId, HTTP 200.
- Nhân viên tạo đơn thành công chuyển `/staff/orders?success=created`; sai dữ liệu/chi tiết/tồn có các mã lỗi invalid/items/stock/create. Đơn khách vãng lai có thể không gắn user_id.
- Đổi sang cancelled hoàn tồn; gửi lại cùng trạng thái không hoàn thêm. Chuyển từ cancelled sang trạng thái khác phải trừ tồn lại đủ hàng; nếu không đủ thì rollback. Chưa có đầy đủ quy tắc thứ tự chuyển giữa các trạng thái còn lại.
- Cập nhật paymentStatus chỉ ghi trạng thái thanh toán. Lựa chọn qr không đồng nghĩa đã tích hợp cổng thanh toán hoặc đã nhận tiền; chưa có callback xác nhận ngân hàng trong luồng này.

### 4.10. Cấu hình máy tính – `/pc-builder`

GET gom linh kiện theo danh mục để chọn cấu hình. POST đưa các lựa chọn hợp lệ vào giỏ hàng của session; không tự tạo đơn hoặc thanh toán.

| Trường | Ràng buộc máy chủ |
| --- | --- |
| productId | Mảng ID lựa chọn; parse số nguyên, bỏ qua ID <= 0, không tồn tại, ngừng hoạt động hoặc hết hàng. |
| quantity | Mảng số lượng theo vị trí; thiếu hoặc sai định dạng mặc định 1; số âm/0 được đưa về 1, số lớn bị giới hạn theo tồn kho. |
| Dòng đã có trong giỏ | Cộng số lượng hiện tại với lượng chọn, tối đa bằng tồn kho. |
| _csrf | Token hợp lệ trước khi cập nhật giỏ. |
| Chuyển trang | Sau POST chuyển HTTP 302 tới `/cart?from=builder`, kể cả khi không có lựa chọn hợp lệ. |

PC Builder hiện chưa áp cùng trần 99 như giỏ/checkout và chưa có kiểm tra bắt buộc tương thích socket CPU, mainboard, RAM hoặc công suất nguồn tại Servlet. Đây là điểm cần chốt tại mục 7, không phải cam kết cấu hình luôn tương thích.

### 4.11. Hóa đơn, báo cáo, lịch sử đơn và chatbot

- **Hóa đơn:** staff/admin mở `/staff/invoice?id=...`; ID không tìm được đơn trả HTTP 404, kể cả ID sai định dạng được parse về 0. Trang hóa đơn lấy chi tiết theo orderId.
- **Lịch sử đơn:** customer chỉ xem danh sách và chi tiết đơn gắn user_id của mình; không tìm thấy/không sở hữu đơn trả HTTP 404. Khách vãng lai không có quyền xem lịch sử customer chỉ bằng mã đơn.
- **Báo cáo:** admin xem doanh thu ngày/tháng, tổng số đơn, tổng khách customer, doanh thu tuần và 30 đơn gần nhất. Servlet chưa nhận tham số chọn ngày tùy ý.
- **Chat:** POST `/ai-chat` nhận message, build và preset; message sau làm sạch tối đa 1.200 ký tự, build cắt tối đa 8.000, câu trả lời cắt tối đa 6.000. Message rỗng/quá dài trả JSON hướng dẫn với HTTP 200; preset hợp lệ có thể trả lời ngay trước bước kiểm tra message.
- **Tư vấn và giới hạn:** tối đa 20 yêu cầu/phút theo địa chỉ IP, đếm theo từng phút của máy chủ; quá ngưỡng trả HTTP 429, Retry-After=60. Có tư vấn preset, AI local và fallback khi AI tắt/lỗi; lưu tối đa 4 lượt lịch sử trong session. Tư vấn không tự tạo đơn, không xác nhận thanh toán và không thay kiểm tra tồn lúc checkout.

## 5. Dữ liệu nghiệp vụ chính

| Nhóm | Thuộc tính chủ yếu |
| --- | --- |
| User | userId, username, password (hash bcrypt), fullName, email, phone, address, role, active, createdAt, lastLogin. |
| Category | catId, catName, description, icon, active. |
| Product | productId, catId, productName, brand, model, description, price, stockQty, specs, imageUrl, active, createdAt. |
| CartItem | product, quantity, subtotal tính từ giá và số lượng; giỏ lưu trong session. |
| Order | orderId, userId có thể null, staffId, customerName, customerPhone, customerEmail, shippingAddr, orderType, totalAmount, discount, finalAmount, status, paymentMethod, paymentStatus, notes, createdAt, updatedAt. |
| OrderItem | itemId, orderId, productId, quantity, unitPrice, subtotal; giá từng dòng lưu theo thời điểm đặt. |
| StockImports | product_id, staff_id, quantity, unit_cost, supplier, note, import_date; cập nhật cùng tồn kho trong transaction. |
| Tư vấn cấu hình | Lựa chọn sản phẩm trong PC Builder; message/build/preset và lịch sử chat theo session, không phải bản ghi đơn hàng đã thanh toán. |

Model chủ yếu lưu dữ liệu, không phải bằng chứng mọi trường đã được validation. Schema demo tại `src/main/resources/gearastral/demo.sql` có khóa chính/ngoại, unique username/email và các CHECK về tiền, tồn, trạng thái; cần xác minh tương ứng trên SQL Server trước nghiệm thu tích hợp.

## 6. Điều kiện và định hướng kiểm thử

### 6.1. Điều kiện môi trường

1. JDK 17 trở lên, Maven và runtime tương thích Jakarta Servlet 6.0; WAR chứa Java cùng `src/main/webapp`. Chạy local tại cổng 8080; trên Tomcat xác nhận context trước khi mở URL.
2. Demo: cấu hình `PCSHOP_DEMO=true` và `PCSHOP_DEMO_STAFF_PASSWORD` đạt chính sách mật khẩu; hệ thống tạo H2 trong bộ nhớ với tài khoản staff `demostaff`. Không ghi mật khẩu thật vào tài liệu/test.
3. SQL Server: mặc định PCShopDB tại localhost:1433; cấu hình `PCSHOP_DB_URL` (hoặc `GEARASTRAL_DB_URL`), `PCSHOP_DB_USER`, `PCSHOP_DB_PASSWORD` hoặc file properties được AppConfig hỗ trợ. Chuẩn bị schema và dữ liệu kiểm thử riêng.
4. Có tài khoản customer/staff/admin, ít nhất hai customer để thử quyền sở hữu, sản phẩm hoạt động/ngừng bán và đủ hàng/hết hàng. Demo chỉ khởi tạo tài khoản staff nêu trên; không giả định có sẵn admin.
5. Tư vấn AI local cần endpoint/model hoạt động; kiểm thử preset/fallback độc lập với dịch vụ AI. H2 là dữ liệu demo theo vòng đời tiến trình; kết quả kiểm thử H2 không tự chứng minh truy vấn SQL Server và triển khai Tomcat đều chạy đúng.

### 6.2. Nhóm ca kiểm thử cần thiết

| Nhóm | Dữ liệu biên / tình huống |
| --- | --- |
| Tài khoản | Trống/blank; username 3/4/32/33; mật khẩu 7/8/9 ký tự và 71/72/73 byte UTF-8; thiếu nhóm ký tự; confirm lệch; username/email trùng khác hoa thường; khóa đăng nhập; đổi session ID. |
| Sản phẩm | Tên 1/2/199/200/201; giá âm/0/1/1.000.000.000/vượt trần/NaN/Infinity; stock -1/0/100.000/100.001; ID sai; ngừng bán; gửi stockQty khi sửa. |
| Phân quyền | GET/POST trực tiếp với khách/customer/staff/admin; CSRF thiếu/sai/khác session; đổi ID xem đơn customer khác; tự toggle tài khoản. |
| Giỏ / PC Builder | Thêm trùng, hết hàng, vượt kho; qty ở giỏ và quantity ở Builder nhận âm/0/1/99/100/chữ; giỏ rỗng; phiên riêng; Builder vượt 99 và checkout sau đó. |
| Đặt hàng / thanh toán | Thông tin nhận thiếu/sai; thay giá trên trình duyệt; tồn thay đổi giữa giỏ và checkout; transaction rollback; COD/QR; gửi lặp; hủy/khôi phục đơn và tồn trước/sau. |
| Danh mục / nhập kho | Biên tên/icon/description; danh mục trùng; quantity 0/1/100.000/100.001; unitCost âm/sai/vượt trần; lỗi SQL khi ghi phiếu/cộng tồn. |
| Tài khoản quản trị / báo cáo / chat | Sửa hồ sơ kèm mật khẩu yếu; email trùng; role không hợp lệ; đối soát báo cáo với dữ liệu chuẩn; message 0/1.200/1.201 ký tự, build dài, preset, rate limit và AI lỗi. |

Đây là định hướng thiết kế test, chưa phải kết quả Pass/Fail. [TEST_PLAN.md](TEST_PLAN.md) và [TEST_CASES.md](TEST_CASES.md) cần dùng URL, dữ liệu và kết quả mong đợi của GearAstral. Unit test dùng mock không thay thế kiểm thử session, bộ lọc, SQL transaction và giao diện.

## 7. Khoảng trống cần hoàn thiện hoặc chốt yêu cầu

| Mã | Phát hiện | Hướng chốt yêu cầu |
| --- | --- | --- |
| GAP-01 | Giá sản phẩm 0 được chấp nhận; chưa kiểm tra tên sản phẩm trùng ở Servlet. | Chốt có cho hàng giá 0/tên trùng không; nếu yêu cầu giá > 0 hoặc tên duy nhất thì sửa validation và test đồng bộ. |
| GAP-02 | Giỏ/checkout giới hạn 99 mỗi dòng, PC Builder chỉ giới hạn theo tồn; chưa xác thực tương thích linh kiện tại Servlet. | Thống nhất trần số lượng; bổ sung kiểm tra tương thích nếu chọn làm yêu cầu nghiệp vụ. |
| GAP-03 | Chuỗi dài thường bị cắt thay vì từ chối; icon danh mục được nhận tới 50 nhưng cột H2 demo chỉ 30. UnitCost nhập kho sai hiện thành 0. | Đồng bộ UI/Servlet/schema, chốt chính sách cắt/từ chối và báo lỗi giá vốn; kiểm thử POST trực tiếp. |
| GAP-04 | Sửa tài khoản lưu hồ sơ trước khi xác thực/reset mật khẩu mới; thao tác có thể cập nhật một phần rồi báo lỗi. | Validation toàn bộ trước ghi; cân nhắc transaction cho hồ sơ và mật khẩu; kiểm tra dữ liệu sau phản hồi lỗi. |
| GAP-05 | Trạng thái đơn có xử lý hoàn/trừ tồn khi hủy nhưng chưa có ma trận chuyển trạng thái đầy đủ; QR chỉ là phương thức được ghi nhận. | Chốt chuyển trạng thái hợp lệ và quy trình xác nhận thanh toán; kiểm thử hủy/khôi phục/lặp và không suy diễn đã tích hợp ngân hàng. |
| GAP-06 | H2 demo và SQL Server có cấu hình, schema và cú pháp khác nhau; không thể suy ra tương thích chỉ từ unit test dùng mock. | Kiểm thử tích hợp trên SQL Server mục tiêu; xác minh khóa/ràng buộc/transaction và dữ liệu tách biệt với demo. |
| GAP-07 | Phạm vi unit test controller và Selenium hiện có chưa bao phủ toàn bộ route/nghiệp vụ trong đặc tả. | Truy vết ca đã có và ca còn thiếu; chỉ ghi Pass khi có kết quả thực chạy, không coi file test hoặc workflow tồn tại là đã nghiệm thu toàn bộ. |

## 8. Nguồn đối chiếu

- **URL/luồng:** `src/main/java/com/pcshop/servlet`, đặc biệt LoginServlet, RegisterServlet, LogoutServlet, ShopServlet, CartServlet, PcBuilderServlet, CustomerOrderServlet và DashboardServlet.
- **Form:** `src/main/webapp/WEB-INF/views`; JavaScript và JSP trong `src/main/webapp`.
- **Validation:** `util/InputValidator.java`, `service/RegistrationService.java`, `servlet/ProductServlet.java`, `UserManageServlet.java`, `CategoryServlet.java`, `StockServlet.java`, `OrderServlet.java` và `AiChatServlet.java`.
- **Quyền/session:** `model/User.java`, `filter/AuthFilter.java`, `filter/SecurityFilter.java`, `util/LoginAttemptGuard.java` và `src/main/webapp/WEB-INF/web.xml`.
- **Giao dịch/dữ liệu:** `dao/OrderDAO.java`, `dao/ProductDAO.java`, `dao/UserDAO.java`, `dao/CategoryDAO.java`, `model`, `util/DBConnection.java`, `util/DemoDatabase.java` và `src/main/resources/gearastral/demo.sql`.
- **Môi trường/test:** `pom.xml`, `src/test/java`, `.github/workflows/test.yml`, `README.md`, `docs/TEST_PLAN.md` và `docs/TEST_CASES.md`.

Quy đổi ký hiệu nghiệp vụ trong bảng truy vết Test Plan hiện có: **§4.A** = mục 4.1–4.3; **§4.B** = mục 4.9; **§4.C** = mục 4.4; **§4.D** = mục 4.8–4.11 (cửa hàng, giỏ, checkout và quyền sở hữu đơn); **§4.E** = mục 4.6–4.7 và phần quản lý đơn ở mục 4.9.
