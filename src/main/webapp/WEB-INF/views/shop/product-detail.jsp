<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${product.productName} - GearAstral" />
<c:set var="pageDescription" value="${product.description}" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>
<c:url var="detailFallbackImage" value="/product-image"><c:param name="id" value="${product.productId}" /></c:url>
<main id="mainContent" class="container product-page">
  <nav class="breadcrumbs" aria-label="Đường dẫn">
    <a href="${pageContext.request.contextPath}/shop">Sản phẩm</a><span>/</span>
    <a href="${pageContext.request.contextPath}/shop?catId=${product.catId}"><c:out value="${product.catName}" /></a><span>/</span>
    <span><c:out value="${product.productName}" /></span>
  </nav>

  <div class="product-detail">
    <figure class="detail-media">
      <img src="${detailFallbackImage}" alt="${fn:escapeXml(product.productName)}" width="900" height="720"
           fetchpriority="high" decoding="async" />
      <figcaption class="product-media-source">
        <span>Ảnh sản phẩm thật theo đúng model</span>
        <a href="${fn:escapeXml(product.officialUrl)}" target="_blank" rel="noopener noreferrer">
          Đối chiếu tại <c:out value="${product.officialSourceName}" /> &#8599;
        </a>
      </figcaption>
    </figure>

    <section class="detail-info">
      <div class="detail-flags">
        <span class="detail-category"><c:out value="${product.catName}" /></span>
        <span class="verified-badge">Thông tin chính hãng</span>
      </div>
      <p class="detail-brand"><c:out value="${product.brand}" /> &middot; <c:out value="${product.model}" /></p>
      <h1><c:out value="${product.productName}" /></h1>
      <p class="detail-description"><c:out value="${product.description}" /></p>
      <div class="detail-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true" />đ</div>
      <div class="detail-stock ${product.stockQty == 0 ? 'out' : ''}">
        <span></span><c:out value="${product.stockStatus}" /> &middot; Kho ${product.stockQty}
      </div>

      <form class="detail-buy" action="${pageContext.request.contextPath}/cart" method="post">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
        <input type="hidden" name="productId" value="${product.productId}" />
        <label for="detailQty">Số lượng</label>
        <input id="detailQty" name="qty" type="number" value="1" min="1" max="${product.stockQty}" />
        <button class="btn" type="submit" ${product.stockQty == 0 ? 'disabled' : ''}>
          ${product.stockQty == 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
        </button>
        <a class="btn secondary" href="${pageContext.request.contextPath}/pc-builder">Dùng trong build PC</a>
      </form>

      <div class="purchase-benefits">
        <div><strong>Sản phẩm chính hãng</strong><span>Đúng thương hiệu, model và thông số công bố</span></div>
        <div><strong>Hỗ trợ lắp ráp</strong><span>Kiểm tra tương thích cấu hình trước khi giao</span></div>
        <div><strong>Theo dõi đơn hàng</strong><span>Cập nhật từng trạng thái xử lý và vận chuyển</span></div>
      </div>
    </section>
  </div>

  <section class="specification-section">
    <div class="specification-heading">
      <span class="eyebrow">THÔNG SỐ KỸ THUẬT</span>
      <h2>Chi tiết sản phẩm</h2>
      <p>Thông tin được trình bày theo đúng model để dễ kiểm tra và so sánh.</p>
      <a class="official-source-link" href="${fn:escapeXml(product.officialUrl)}" target="_blank" rel="noopener noreferrer">
        Xem nguồn <c:out value="${product.officialSourceName}" /> &#8599;
      </a>
    </div>
    <dl class="specification-list">
      <c:forEach var="spec" items="${product.specificationItems}">
        <dt><c:out value="${spec.label}" /></dt>
        <dd><c:out value="${spec.value}" /></dd>
      </c:forEach>
      <dt>Tồn kho hiện tại</dt><dd>${product.stockQty} sản phẩm</dd>
    </dl>
  </section>

  <c:if test="${not empty related}">
    <section class="related-section">
      <div class="section-title">
        <div><span class="eyebrow">CÙNG DANH MỤC</span><h2>Sản phẩm tương tự</h2></div>
        <a href="${pageContext.request.contextPath}/shop?catId=${product.catId}">Xem tất cả</a>
      </div>
      <div class="product-grid related-grid">
        <c:set var="productCardEager" value="false" />
        <c:forEach var="p" items="${related}"><%@ include file="_product-card.jspf" %></c:forEach>
        <c:remove var="productCardEager" />
      </div>
    </section>
  </c:if>
</main>
<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
</body>
</html>
