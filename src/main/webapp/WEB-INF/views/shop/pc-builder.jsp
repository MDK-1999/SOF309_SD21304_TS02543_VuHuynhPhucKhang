<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Tự build PC - GearAstral" />
<c:set var="pageDescription" value="Tự chọn linh kiện, tính tổng giá và kiểm tra tương thích cấu hình PC." />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="_head.jspf" %>
</head>
<body>
<%@ include file="_header.jspf" %>
<main id="mainContent" class="container section page-section builder-page">
  <div class="section-title">
    <div><span class="eyebrow">PC BUILDER</span><h1>Tự chọn cấu hình PC</h1><p>Chọn từng linh kiện để xem tổng giá, công suất và các cảnh báo tương thích.</p></div>
    <div class="title-actions"><button class="btn secondary" id="resetBuilder" type="button">Đặt lại</button><a class="btn secondary" href="${pageContext.request.contextPath}/shop">Xem sản phẩm</a></div>
  </div>

  <ol class="process-steps" aria-label="Quy trình build PC">
    <li class="active"><span>1</span><div><strong>Chọn linh kiện</strong><small>Giá và tồn kho hiện tại</small></div></li>
    <li><span>2</span><div><strong>Kiểm tra cấu hình</strong><small>Socket, RAM và công suất</small></div></li>
    <li><span>3</span><div><strong>Thêm vào giỏ</strong><small>Chốt số lượng từng món</small></div></li>
  </ol>
  <form class="builder-shell" action="${pageContext.request.contextPath}/pc-builder" method="post" id="builderForm">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
    <section class="builder-parts" aria-label="Danh sách linh kiện">
      <c:forEach var="entry" items="${builderParts}">
        <article class="builder-part" data-category="${entry.key.catId}">
          <div class="part-preview" data-part-preview><span>${entry.key.icon}</span></div>
          <div class="part-copy"><span>Bước ${entry.key.catId}</span><h2>${entry.key.catName}</h2><p>${entry.key.description}</p></div>
          <div class="part-control">
            <label class="sr-only" for="part-${entry.key.catId}">Chọn ${entry.key.catName}</label>
            <select id="part-${entry.key.catId}" name="productId" class="part-select" data-category="${entry.key.catId}">
              <option value="" data-price="0" data-name="Chưa chọn" data-brand="" data-model="" data-specs="" data-image="" data-stock="0">Chưa chọn linh kiện</option>
              <c:forEach var="p" items="${entry.value}">
                <option value="${p.productId}" data-price="${p.price}" data-name="${fn:escapeXml(p.productName)}"
                        data-brand="${fn:escapeXml(p.brand)}" data-model="${fn:escapeXml(p.model)}" data-specs="${fn:escapeXml(p.specs)}"
                        data-image="${pageContext.request.contextPath}/product-image?id=${p.productId}" data-stock="${p.stockQty}">
                  ${p.productName} · <fmt:formatNumber value="${p.price}" type="number" groupingUsed="true" />đ
                </option>
              </c:forEach>
            </select>
            <div class="part-actions" data-part-actions hidden>
              <div class="part-quantity" aria-label="Điều chỉnh số lượng">
                <button type="button" data-qty-action="decrease" aria-label="Giảm số lượng">−</button>
                <label class="sr-only" for="quantity-${entry.key.catId}">Số lượng ${entry.key.catName}</label>
                <input id="quantity-${entry.key.catId}" name="quantity" type="number" value="1" min="1" max="1"
                       inputmode="numeric" data-part-quantity aria-label="Số lượng ${entry.key.catName}" />
                <button type="button" data-qty-action="increase" aria-label="Tăng số lượng">+</button>
              </div>
              <button class="part-remove" type="button" data-remove-part>Xóa</button>
              <button class="part-change" type="button" data-change-part>Sửa / thay đổi</button>
            </div>
            <span class="part-selected-price" data-part-price>Chưa chọn</span>
          </div>
        </article>
      </c:forEach>
    </section>

    <aside class="builder-summary">
      <div class="summary-heading"><div><span class="eyebrow">CẤU HÌNH CỦA BẠN</span><h2>Tổng quan build</h2></div><span class="summary-count" id="selectedCount">0 món</span></div>
      <div class="build-stats">
        <div class="build-stat"><span>Tổng giá</span><strong id="buildTotal">0đ</strong></div>
        <div class="build-stat"><span>Tương thích</span><strong id="compatStatus">Đang chọn</strong></div>
        <div class="build-stat"><span>Công suất tải</span><strong id="wattStatus">0W</strong><small id="psuRecommend">Chưa đủ dữ liệu</small></div>
      </div>
      <div class="compatibility-box" id="compatibilityBox">Hãy chọn CPU, mainboard, RAM, GPU, SSD và nguồn để kiểm tra cấu hình.</div>
      <div class="selected-list" id="selectedList"><div class="selected-empty">Chưa có linh kiện nào được chọn.</div></div>
      <div class="advisor-box" id="advisorBox">GearAstral sẽ gợi ý mức cấu hình phù hợp theo tổng ngân sách của bạn.</div>
      <button class="btn checkout-button" id="addBuildButton" type="submit" disabled>Thêm cấu hình vào giỏ</button>
      <a class="btn secondary builder-cart-link" href="${pageContext.request.contextPath}/cart">Xem giỏ hàng</a>
      <p class="builder-note">Giá và tồn kho được lấy trực tiếp từ danh mục sản phẩm hiện tại.</p>
    </aside>
  </form>
</main>
<%@ include file="_contact.jspf" %>
<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/pc-builder.js?v=7.0.0" defer></script>
</body>
</html>
