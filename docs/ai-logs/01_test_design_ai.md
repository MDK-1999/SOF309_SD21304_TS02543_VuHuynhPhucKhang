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
