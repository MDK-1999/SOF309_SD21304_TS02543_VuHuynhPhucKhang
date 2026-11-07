<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Thanh toán - GearAstral" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>
<main id="mainContent" class="container section page-section">
  <div class="section-title"><div><span class="eyebrow">HOÀN TẤT ĐẶT HÀNG</span><h1>Thông tin giao hàng</h1><p>GearAstral sẽ xác nhận tồn kho và thời gian giao ngay sau khi nhận đơn.</p></div></div>
  <ol class="process-steps checkout-steps" aria-label="Tiến trình đặt hàng">
    <li class="completed"><span>✓</span><div><strong>Giỏ hàng</strong><small>Đã kiểm tra sản phẩm</small></div></li>
    <li class="active"><span>2</span><div><strong>Giao hàng</strong><small>Địa chỉ và liên hệ</small></div></li>
    <li><span>3</span><div><strong>Xác nhận</strong><small>Thanh toán khi nhận hàng</small></div></li>
  </ol>
  <c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}" /></div></c:if>

  <form class="checkout-grid" action="${pageContext.request.contextPath}/checkout" method="post">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
    <section class="checkout-panel">
      <input type="hidden" name="action" value="checkout" />
      <div class="form-section-title"><span>01</span><div><h2>Người nhận</h2><p>Nhập thông tin dùng để giao và xác nhận đơn.</p></div></div>
      <div class="form-grid">
        <div class="field"><label for="customerName">Họ và tên</label><input id="customerName" name="customerName" value="${fn:escapeXml(sessionScope.loginUser.fullName)}" autocomplete="name" minlength="2" maxlength="100" required /></div>
        <div class="field"><label for="customerPhone">Số điện thoại</label><input id="customerPhone" name="customerPhone" value="${fn:escapeXml(sessionScope.loginUser.phone)}" autocomplete="tel" inputmode="tel" maxlength="15" required /></div>
        <div class="field form-full"><label for="customerEmail">Email</label><input id="customerEmail" name="customerEmail" type="email" value="${fn:escapeXml(sessionScope.loginUser.email)}" autocomplete="email" maxlength="100" /></div>
        <div class="field form-full"><label for="shippingAddr">Địa chỉ giao hàng</label><textarea id="shippingAddr" name="shippingAddr" rows="3" autocomplete="street-address" maxlength="200" required>${fn:escapeXml(sessionScope.loginUser.address)}</textarea></div>
      </div>

      <div class="form-section-title"><span>02</span><div><h2>Phương thức thanh toán</h2><p>Đơn demo sử dụng hình thức thanh toán khi nhận hàng.</p></div></div>
      <div class="payment-options">
        <label class="payment-option active"><input type="radio" name="paymentMethod" value="cod" checked /><span><strong>Thanh toán khi nhận hàng</strong><small>Thanh toán trực tiếp khi đơn được giao tới.</small></span></label>
      </div>
      <div class="field checkout-note"><label for="notes">Ghi chú cho đơn hàng</label><textarea id="notes" name="notes" rows="3" maxlength="500" placeholder="Nhu cầu lắp ráp, xuất hóa đơn hoặc thời gian nhận hàng..."></textarea></div>
    </section>

    <aside class="order-summary checkout-summary">
      <h2>Đơn hàng của bạn</h2>
      <div class="checkout-products">
        <c:forEach var="item" items="${cartItems}">
          <div class="checkout-product"><img src="${pageContext.request.contextPath}/product-image?id=${item.product.productId}" alt="${fn:escapeXml(item.product.productName)}" width="64" height="64" loading="lazy" decoding="async" /><div><strong><c:out value="${item.product.productName}" /></strong><span>${item.quantity} × <fmt:formatNumber value="${item.product.price}" type="number" groupingUsed="true" />đ</span></div><b><fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ</b></div>
        </c:forEach>
      </div>
      <div><span>Tạm tính</span><strong><fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true" />đ</strong></div>
      <div><span>Phí giao hàng</span><span>Xác nhận sau</span></div>
      <div class="summary-total"><span>Tổng tạm tính</span><strong><fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true" />đ</strong></div>
      <button class="btn checkout-button" type="submit">Xác nhận đặt hàng</button>
      <p>Bằng việc đặt hàng, bạn đồng ý để GearAstral liên hệ xác nhận thông tin đơn.</p>
    </aside>
  </form>
</main>
<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
</body>
</html>
