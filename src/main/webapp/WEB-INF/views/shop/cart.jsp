<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Giỏ hàng - GearAstral" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>
<main id="mainContent" class="container section page-section">
  <div class="section-title">
    <div><span class="eyebrow">ĐƠN HÀNG CỦA BẠN</span><h1>Giỏ hàng</h1><p>Kiểm tra sản phẩm và số lượng trước khi đặt hàng.</p></div>
    <a class="btn secondary" href="${pageContext.request.contextPath}/shop">Tiếp tục mua</a>
  </div>
  <ol class="process-steps checkout-steps" aria-label="Tiến trình đặt hàng">
    <li class="active"><span>1</span><div><strong>Giỏ hàng</strong><small>Kiểm tra số lượng</small></div></li>
    <li><span>2</span><div><strong>Giao hàng</strong><small>Địa chỉ và liên hệ</small></div></li>
    <li><span>3</span><div><strong>Xác nhận</strong><small>Thanh toán khi nhận hàng</small></div></li>
  </ol>
  <div class="checkout-grid">
    <section class="cart-items">
      <c:choose>
        <c:when test="${empty cartItems}">
          <div class="empty-products"><strong>Giỏ hàng đang trống</strong><span>Hãy chọn linh kiện hoặc tạo một cấu hình PC theo nhu cầu.</span><a class="btn" href="${pageContext.request.contextPath}/pc-builder">Tự build PC</a></div>
        </c:when>
        <c:otherwise>
          <c:forEach var="item" items="${cartItems}">
            <article class="cart-item">
              <img src="${pageContext.request.contextPath}/product-image?id=${item.product.productId}" alt="${item.product.productName}" width="112" height="112" loading="lazy" decoding="async" />
              <div class="cart-item-info"><span>${item.product.brand} · ${item.product.catName}</span><a href="${pageContext.request.contextPath}/product?id=${item.product.productId}">${item.product.productName}</a><small>${item.product.specs}</small></div>
              <div class="cart-unit"><span>Đơn giá</span><strong><fmt:formatNumber value="${item.product.price}" type="number" groupingUsed="true" />đ</strong></div>
              <form class="cart-quantity" action="${pageContext.request.contextPath}/cart" method="post">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
                <input type="hidden" name="action" value="update" /><input type="hidden" name="productId" value="${item.product.productId}" />
                <label for="qty-${item.product.productId}">Số lượng</label><input id="qty-${item.product.productId}" name="qty" type="number" min="1" max="${item.product.stockQty}" value="${item.quantity}" />
                <button type="submit">Cập nhật</button>
              </form>
              <div class="cart-subtotal"><span>Tạm tính</span><strong><fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ</strong></div>
              <form class="cart-remove" action="${pageContext.request.contextPath}/cart" method="post">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
                <input type="hidden" name="action" value="remove" /><input type="hidden" name="productId" value="${item.product.productId}" />
                <button type="submit" aria-label="Xóa ${item.product.productName}">Xóa</button>
              </form>
            </article>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </section>
    <aside class="order-summary">
      <h2>Tổng đơn hàng</h2>
      <div><span>Tạm tính</span><strong><fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true" />đ</strong></div>
      <div><span>Phí giao hàng</span><span>Xác nhận sau</span></div>
      <div class="summary-total"><span>Tổng tạm tính</span><strong><fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true" />đ</strong></div>
      <p>GearAstral sẽ gọi xác nhận cấu hình, tồn kho và thời gian giao hàng.</p>
      <c:choose>
        <c:when test="${empty cartItems}"><span class="btn checkout-button disabled">Tiến hành đặt hàng</span></c:when>
        <c:otherwise><a class="btn checkout-button" href="${pageContext.request.contextPath}/checkout">Tiến hành đặt hàng</a></c:otherwise>
      </c:choose>
      <div class="summary-security"><strong>Thanh toán an toàn</strong><span>Hỗ trợ thanh toán khi nhận hàng.</span></div>
    </aside>
  </div>
</main>
<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
</body>
</html>
