<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn của tôi - GearAstral" />
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
      <span class="eyebrow">TÀI KHOẢN KHÁCH HÀNG</span>
      <h1>Đơn hàng của tôi</h1>
      <p class="specs" style="display:block">Theo dõi tổng chi tiêu, trạng thái xử lý và tiến trình giao hàng.</p>
    </div>
    <a class="btn secondary small" href="${pageContext.request.contextPath}/shop">Mua thêm</a>
  </div>
  <div class="order-metrics">
    <div class="metric"><span>Tổng đơn</span><strong>${orderCount}</strong></div>
    <div class="metric"><span>Đang xử lý/giao</span><strong>${activeOrders}</strong></div>
    <div class="metric"><span>Tổng đã đặt</span><strong><fmt:formatNumber value="${totalSpent}" type="number" groupingUsed="true" />đ</strong></div>
  </div>
  <div class="table-scroll"><table class="table">
    <thead>
      <tr><th>Mã đơn</th><th>Ngày đặt</th><th>Người nhận</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th></th></tr>
    </thead>
    <tbody>
      <c:forEach var="o" items="${orders}">
        <tr>
          <td><strong>#${o.orderId}</strong></td>
          <td>${o.createdAt}</td>
          <td>${o.customerName}<br/><span class="specs" style="display:block">${o.customerPhone}</span></td>
          <td><strong><fmt:formatNumber value="${o.finalAmount}" type="number" groupingUsed="true" />đ</strong></td>
          <td><span class="badge"><c:choose><c:when test="${o.paymentMethod == 'qr'}">Thanh toán QR</c:when><c:otherwise>COD</c:otherwise></c:choose> / ${o.paymentStatus}</span></td>
          <td><span class="badge"><c:choose><c:when test="${o.status == 'pending'}">Chờ xử lý</c:when><c:when test="${o.status == 'confirmed'}">Đã xác nhận</c:when><c:when test="${o.status == 'processing'}">Đang chuẩn bị</c:when><c:when test="${o.status == 'shipping'}">Đang giao hàng</c:when><c:when test="${o.status == 'done'}">Hoàn thành</c:when><c:when test="${o.status == 'cancelled'}">Đã hủy</c:when><c:otherwise>${o.status}</c:otherwise></c:choose></span></td>
          <td><a class="btn small" href="${pageContext.request.contextPath}/customer/orders?id=${o.orderId}">Chi tiết</a></td>
        </tr>
      </c:forEach>
      <c:if test="${empty orders}">
        <tr><td colspan="7" style="text-align:center;padding:28px">Bạn chưa có đơn hàng nào.</td></tr>
      </c:if>
    </tbody>
  </table></div>
</main>
<%@ include file="/WEB-INF/views/shop/_contact.jspf" %>
<%@ include file="/WEB-INF/views/shop/_footer.jspf" %>
</body>
</html>
