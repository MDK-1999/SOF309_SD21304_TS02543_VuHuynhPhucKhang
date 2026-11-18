# Nhật ký AI · Thiết kế kiểm thử GearAstral

**SOF309 · Vũ Huỳnh Phúc Khang · TS02543 · 07/11/2026**

## Ngữ cảnh

Đưa [đặc tả GearAstral](../SPECIFICATIONS.md) vào ngữ cảnh của Codex. Đối chiếu với `RegisterServlet`, `LoginServlet`, `ProductServlet`, `AuthFilter`, `SecurityFilter`, DAO và biểu mẫu JSP.

## Prompt

> Bạn là QA Lead. Dựa trên đặc tả GearAstral được cung cấp, thiết kế Test Plan và Test Case cho đăng ký, đăng nhập, phân quyền và quản lý sản phẩm. Áp dụng phân vùng tương đương và phân tích giá trị biên. Tách yêu cầu HTTP của Controller khỏi thao tác giao diện. Ghi rõ tên trường, URL, CSRF, vai trò, kết quả mong đợi, chuyển hướng và cách kiểm tra dữ liệu sau khi thêm, sửa hoặc ngừng bán sản phẩm. Phân biệt ca đã thực hiện với ca chỉ được thiết kế.

## Đối chiếu đầu ra

- Đã ghi [kế hoạch](../TEST_PLAN.md) và [ca kiểm thử](../TEST_CASES.md) theo route thật của dự án.
- Bổ sung biên 72 byte UTF-8 vì GearAstral dùng bcrypt. Ghi rõ khi sai dữ liệu sản phẩm, ứng dụng chuyển hướng kèm tham số lỗi.
- Dùng thao tác Ngừng bán cho ca xóa logic. Kiểm tra cả bảng nhân viên, danh mục và chi tiết công khai.

## Phiên đối chiếu bố cục ngày 18/11/2026

**Yêu cầu của người dùng**

> Bạn vào link này và làm lại GitHub cho tôi dựa vào GearAstral mà làm, link kia chỉ là mẫu.

Đầu vào gồm repository tham khảo Thảo Nguyên, tài liệu GearAstral và mã nguồn `AuthFilter`, `SecurityFilter`, `ShopServlet`, `CartServlet`, `CustomerOrderServlet`, `OrderServlet`, `StockServlet`, `CategoryServlet`.

### Đầu ra

- Bổ sung mục vai trò và ma trận URL vào đặc tả, với route thực như `/shop`, `/register`, `/staff/products` và `/customer/orders`.
- Bổ sung giỏ hàng, giá hiện tại khi checkout, lượng tối đa 99, quyền sở hữu đơn, nhập kho, trạng thái đơn và thanh toán.
- Bổ sung fixture, ma trận quyền, tiêu chí vào/ra và bảng truy vết sang hai class Unit Test.
- Bổ sung các ca HTTP/tích hợp/UI cho giai đoạn tiếp theo với trạng thái Chưa chạy.
- Tổng hợp 40 lượt Unit Test đã chạy ở mục 5 Test Cases. Kết quả HTTP ngày 07/11 được giữ theo bằng chứng lịch sử.

### Đối chiếu nghiệp vụ

GearAstral dùng Java Servlet/JSP, bcrypt, H2 demo hoặc SQL Server. Biên username 4–32 và mật khẩu tối đa 72 byte được lấy từ `InputValidator`. Giá sản phẩm cho phép 0 theo code. Giỏ nhận lượng sai bằng fallback 1. Thanh toán web dùng COD. Quy tắc từ source được ghi rõ trong đặc tả và expected của từng ca.

### Phần người học rà soát

Người học cần xác nhận fixture DB, chạy các ca mở rộng và ghi Actual/Evidence sau thực thi. Nhật ký này ghi phần AI đã xử lý trong phiên, cùng các điều kiện còn cần nghiệm thu.
