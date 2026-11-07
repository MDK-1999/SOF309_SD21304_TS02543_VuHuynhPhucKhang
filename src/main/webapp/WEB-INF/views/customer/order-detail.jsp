<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Chi tiết đơn #${order.orderId} - GearAstral" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="/WEB-INF/views/shop/_head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/shop/_header.jspf" %>
<main class="container section page-section">
  <div class="section-title">
    <div>
      <span class="eyebrow">THEO DÕI ĐƠN HÀNG</span>
      <h1>Đơn hàng #${order.orderId}</h1>
      <p class="specs" style="display:block">Người nhận: ${order.customerName} - ${order.customerPhone}</p>
    </div>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/customer/orders">Quay lại</a>
  </div>
  <c:set var="s" value="${order.status}" />
  <div class="cart-summary" style="background:#fff">
    <h3>Quá trình giao hàng</h3>
    <div class="status-flow">
      <div class="flow-step ${s != 'cancelled' ? 'active' : 'cancelled'}">Chờ xác nhận</div>
      <div class="flow-step ${s == 'confirmed' || s == 'processing' || s == 'shipping' || s == 'done' ? 'active' : ''}">Đã xác nhận</div>
      <div class="flow-step ${s == 'processing' || s == 'shipping' || s == 'done' ? 'active' : ''}">Đang chuẩn bị</div>
      <div class="flow-step ${s == 'shipping' || s == 'done' ? 'active' : ''}">Đang giao</div>
      <div class="flow-step ${s == 'done' ? 'active' : ''}">Hoàn thành</div>
    </div>
    <c:if test="${s == 'cancelled'}"><p style="color:#991b1b;font-weight:700">Đơn hàng đã bị hủy.</p></c:if>
  </div>
  <div class="checkout-grid" style="margin-top:18px">
    <section>
      <table class="table">
        <thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Tạm tính</th></tr></thead>
        <tbody>
          <c:forEach var="item" items="${order.items}">
            <tr>
              <td><strong>${item.productName}</strong></td>
              <td>${item.quantity}</td>
              <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" />đ</td>
              <td><strong><fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ</strong></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </section>
    <aside class="cart-summary">
      <h3>Tổng đơn</h3>
      <p>Tạm tính: <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" />đ</strong></p>
      <p>Giảm giá: <strong><fmt:formatNumber value="${order.discount}" type="number" groupingUsed="true" />đ</strong></p>
      <div class="price"><fmt:formatNumber value="${order.finalAmount}" type="number" groupingUsed="true" />đ</div>
      <p><strong>Thanh toán:</strong> <c:choose><c:when test="${order.paymentMethod == 'qr'}">Thanh toán QR</c:when><c:otherwise>COD</c:otherwise></c:choose> - ${order.paymentStatus}</p>
      <p><strong>Địa chỉ:</strong> ${order.shippingAddr}</p>
      <p><strong>Ghi chú:</strong> ${order.notes}</p>
    </aside>
  </div>
</main>
<%@ include file="/WEB-INF/views/shop/_contact.jspf" %>
<%@ include file="/WEB-INF/views/shop/_footer.jspf" %>
</body>
</html>
