<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral - Linh kiện PC chính hãng" />
<c:set var="pageDescription" value="Linh kiện PC chính hãng, giá rõ ràng, tự build và kiểm tra cấu hình tại GearAstral." />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>

<section class="catalog-banner">
  <div class="container catalog-banner-inner">
    <div class="catalog-banner-copy">
      <div class="catalog-breadcrumb"><span>GEARASTRAL</span><i></i><span>PERFORMANCE CATALOG 2020-2026</span></div>
      <h1>Build hi&#7879;u n&#259;ng cao,<br/>ch&#7885;n &#273;&#250;ng ngay t&#7915; &#273;&#7847;u.</h1>
      <p>Catalog linh ki&#7879;n ch&#237;nh h&#227;ng v&#7899;i &#7843;nh s&#7843;n ph&#7849;m th&#7853;t, gi&#225; b&#225;n v&#224; t&#7891;n kho minh b&#7841;ch. M&#7885;i c&#7845;u h&#236;nh &#273;&#7873;u c&#243; th&#7875; ki&#7875;m tra t&#432;&#417;ng th&#237;ch tr&#432;&#7899;c khi &#273;&#7863;t.</p>
      <div class="catalog-highlights">
        <span><strong>${totalProducts}</strong> s&#7843;n ph&#7849;m ph&#249; h&#7907;p</span>
        <span>D&#7919; li&#7879;u t&#7891;n kho hi&#7879;n t&#7841;i</span>
        <span>L&#7855;p r&#225;p v&#224; ki&#7875;m tra &#7893;n &#273;&#7883;nh</span>
      </div>
    </div>
    <div class="catalog-command">
      <span class="command-label">PC CONFIGURATOR</span>
      <strong>X&#226;y c&#7845;u h&#236;nh theo ng&#226;n s&#225;ch</strong>
      <p>Ch&#7885;n linh ki&#7879;n, ch&#7881;nh s&#7889; l&#432;&#7907;ng v&#224; xem t&#7893;ng gi&#225; trong c&#249;ng m&#7897;t quy tr&#236;nh.</p>
      <a class="btn builder-cta" href="${pageContext.request.contextPath}/pc-builder">B&#7855;t &#273;&#7847;u build PC</a>
      <a class="catalog-text-link" href="#catalogResults">Ho&#7863;c duy&#7879;t catalog <span aria-hidden="true">&#8595;</span></a>
    </div>
  </div>
</section>


<c:if test="${not empty featured}">
  <section class="featured-shelf" aria-labelledby="featuredTitle">
    <div class="container">
      <div class="featured-heading">
        <div><span>EDITOR'S SELECTION</span><h2 id="featuredTitle">Linh ki&#7879;n m&#7899;i &#273;&#225;ng ch&#250; &#253;</h2></div>
        <a href="#catalogResults">Xem to&#224;n b&#7897; catalog</a>
      </div>
      <div class="featured-grid">
        <c:forEach var="fp" items="${featured}" begin="0" end="3" varStatus="status">
          <c:url var="featuredImage" value="/product-image"><c:param name="id" value="${fp.productId}" /></c:url>
          <a class="featured-product" href="${pageContext.request.contextPath}/product?id=${fp.productId}">
            <span class="featured-number">0${status.count}</span>
            <img src="${featuredImage}" alt="" loading="${status.index lt 2 ? 'eager' : 'lazy'}"
                 fetchpriority="${status.index lt 2 ? 'high' : 'auto'}" decoding="async" width="640" height="480" />
            <span class="featured-copy"><small><c:out value="${fp.brand}" /> &#183; <c:out value="${fp.catName}" /></small><strong><c:out value="${fp.productName}" /></strong><b><fmt:formatNumber value="${fp.price}" type="number" groupingUsed="true" />&#273;</b></span>
          </a>
        </c:forEach>
      </div>
    </div>
  </section>
</c:if>
<section class="trust-strip" aria-label="Cam kết dịch vụ">
  <div class="container trust-grid">
    <div><span class="trust-index">01</span><strong>Tư vấn đúng nhu cầu</strong><small>Gaming, đồ họa, AI và văn phòng</small></div>
    <div><span class="trust-index">02</span><strong>Giá rõ từng linh kiện</strong><small>Tổng build được tính tức thời</small></div>
    <div><span class="trust-index">03</span><strong>Lắp ráp và kiểm tra</strong><small>Test ổn định trước khi giao</small></div>
    <div><span class="trust-index">04</span><strong>Hỗ trợ sau bán</strong><small>Theo dõi tiến độ từng đơn hàng</small></div>
  </div>
</section>

<main id="mainContent">
<section class="container catalog-section" id="catalogResults">
  <div class="category-links" aria-label="Lọc nhanh theo danh mục">
    <a class="${empty param.catId ? 'active' : ''}" href="${pageContext.request.contextPath}/shop">Tất cả</a>
    <c:forEach var="cat" items="${categories}">
      <a class="${param.catId == cat.catId ? 'active' : ''}"
         href="${pageContext.request.contextPath}/shop?catId=${cat.catId}"><c:out value="${cat.catName}" /></a>
    </c:forEach>
  </div>

  <div class="catalog-heading">
    <div>
      <h2>Danh sách sản phẩm</h2>
      <p>Tìm thấy <strong>${totalProducts}</strong> sản phẩm phù hợp</p>
    </div>
    <form class="sort-form" action="${pageContext.request.contextPath}/shop" method="get">
      <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${fn:escapeXml(param.q)}" /></c:if>
      <c:if test="${not empty param.catId}"><input type="hidden" name="catId" value="${param.catId}" /></c:if>
      <c:if test="${not empty param.brand}"><input type="hidden" name="brand" value="${fn:escapeXml(param.brand)}" /></c:if>
      <c:if test="${not empty param.min}"><input type="hidden" name="min" value="${fn:escapeXml(param.min)}" /></c:if>
      <c:if test="${not empty param.max}"><input type="hidden" name="max" value="${fn:escapeXml(param.max)}" /></c:if>
      <label for="sort">Sắp xếp</label>
      <select id="sort" name="sort" onchange="this.form.submit()">
        <option value="" ${empty param.sort ? 'selected' : ''}>Mới cập nhật</option>
        <option value="price-asc" ${param.sort == 'price-asc' ? 'selected' : ''}>Giá thấp đến cao</option>
        <option value="price-desc" ${param.sort == 'price-desc' ? 'selected' : ''}>Giá cao đến thấp</option>
        <option value="stock" ${param.sort == 'stock' ? 'selected' : ''}>Còn nhiều hàng</option>
        <option value="name" ${param.sort == 'name' ? 'selected' : ''}>Tên A-Z</option>
      </select>
    </form>
  </div>

  <div class="catalog-layout">
    <aside class="filter-panel">
      <div class="filter-title"><span><small>CATALOG TOOLS</small><strong>Bộ lọc sản phẩm</strong></span><a href="${pageContext.request.contextPath}/shop">Xóa lọc</a></div>
      <form action="${pageContext.request.contextPath}/shop" method="get">
        <input type="hidden" name="sort" value="${fn:escapeXml(param.sort)}" />
        <div class="field">
          <label for="filterQ">Từ khóa</label>
          <input id="filterQ" name="q" value="${fn:escapeXml(param.q)}" placeholder="Ví dụ: RTX 5070" />
        </div>
        <div class="field">
          <label for="filterCategory">Danh mục</label>
          <select id="filterCategory" name="catId">
            <option value="">Tất cả danh mục</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.catId}" ${param.catId == cat.catId ? 'selected' : ''}><c:out value="${cat.catName}" /></option>
            </c:forEach>
          </select>
        </div>
        <div class="field">
          <label for="filterBrand">Hãng</label>
          <select id="filterBrand" name="brand">
            <option value="">Tất cả hãng</option>
            <c:forEach var="brand" items="${brands}">
              <option value="${fn:escapeXml(brand)}" ${param.brand == brand ? 'selected' : ''}><c:out value="${brand}" /></option>
            </c:forEach>
          </select>
        </div>
        <div class="price-filter">
          <div class="field"><label for="minPrice">Giá từ</label><input id="minPrice" name="min" type="number" min="0" value="${fn:escapeXml(param.min)}" placeholder="1.000.000" /></div>
          <div class="field"><label for="maxPrice">Giá đến</label><input id="maxPrice" name="max" type="number" min="0" value="${fn:escapeXml(param.max)}" placeholder="50.000.000" /></div>
        </div>
        <button class="btn filter-submit" type="submit">Áp dụng bộ lọc</button>
      </form>
      <div class="filter-support"><strong>Cần chọn nhanh?</strong><span>Gọi 0901 552 900 để được tư vấn theo ngân sách.</span></div>
    </aside>

    <section aria-label="Kết quả sản phẩm">
      <div class="product-grid">
        <c:forEach var="p" items="${products}" varStatus="productStatus">
          <c:set var="productCardEager" value="${productStatus.index lt 4}" />
          <%@ include file="_product-card.jspf" %>
        </c:forEach>
        <c:remove var="productCardEager" />
      </div>
      <c:if test="${empty products}">
        <div class="empty-products"><strong>Chưa tìm thấy sản phẩm phù hợp</strong><span>Hãy thử đổi từ khóa hoặc bỏ bớt điều kiện lọc.</span><a class="btn" href="${pageContext.request.contextPath}/shop">Xem tất cả sản phẩm</a></div>
      </c:if>

      <c:if test="${totalPages gt 1}">
        <nav class="pagination" aria-label="Phân trang sản phẩm">
          <c:if test="${page gt 1}">
            <c:url var="previousUrl" value="/shop"><c:param name="q" value="${param.q}"/><c:param name="catId" value="${param.catId}"/><c:param name="brand" value="${param.brand}"/><c:param name="min" value="${param.min}"/><c:param name="max" value="${param.max}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${page - 1}"/></c:url>
            <a class="page-link page-direction" href="${previousUrl}" aria-label="Trang trước">Trước</a>
          </c:if>
          <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
            <c:url var="pageUrl" value="/shop"><c:param name="q" value="${param.q}"/><c:param name="catId" value="${param.catId}"/><c:param name="brand" value="${param.brand}"/><c:param name="min" value="${param.min}"/><c:param name="max" value="${param.max}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${pageNumber}"/></c:url>
            <a class="page-link ${pageNumber eq page ? 'active' : ''}" href="${pageUrl}"
               aria-current="${pageNumber eq page ? 'page' : 'false'}">${pageNumber}</a>
          </c:forEach>
          <c:if test="${page lt totalPages}">
            <c:url var="nextUrl" value="/shop"><c:param name="q" value="${param.q}"/><c:param name="catId" value="${param.catId}"/><c:param name="brand" value="${param.brand}"/><c:param name="min" value="${param.min}"/><c:param name="max" value="${param.max}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${page + 1}"/></c:url>
            <a class="page-link page-direction" href="${nextUrl}" aria-label="Trang sau">Sau</a>
          </c:if>
        </nav>
      </c:if>
    </section>
  </div>
</section>
</main>

<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
</body>
</html>
