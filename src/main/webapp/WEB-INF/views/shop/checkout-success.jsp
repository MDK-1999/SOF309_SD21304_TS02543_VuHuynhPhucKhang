<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đặt hàng thành công - GearAstral" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>
<main class="container section page-section">
  <ol class="process-steps checkout-steps success-steps" aria-label="Tiến trình đặt hàng">
    <li class="completed"><span>✓</span><div><strong>Giỏ hàng</strong><small>Đã kiểm tra sản phẩm</small></div></li>
    <li class="completed"><span>✓</span><div><strong>Giao hàng</strong><small>Đã nhận thông tin</small></div></li>
    <li class="active"><span>3</span><div><strong>Xác nhận</strong><small>Đơn hàng đã được tạo</small></div></li>
  </ol>
  <div class="success-panel">
    <span class="success-mark">✓</span>
    <span class="eyebrow">ĐẶT HÀNG THÀNH CÔNG</span>
    <h1>Cảm ơn bạn đã chọn GearAstral</h1>
    <p>Mã đơn của bạn là <strong>#${orderId}</strong>. GearAstral sẽ liên hệ xác nhận cấu hình, tồn kho và thời gian giao hàng.</p>
    <div class="success-actions">
      <a class="btn" href="${pageContext.request.contextPath}/shop">Tiếp tục mua hàng</a>
      <c:if test="${not empty sessionScope.loginUser}">
        <a class="btn secondary" href="${pageContext.request.contextPath}/customer/orders?id=${orderId}">Theo dõi đơn hàng</a>
      </c:if>
    </div>
  </div>
</main>
<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
</body>
</html>
