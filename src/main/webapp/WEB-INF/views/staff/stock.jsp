<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Kho hàng" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">🏪 Quản lý Kho hàng</div>
      <div class="topbar-right">
        <button class="btn btn-primary btn-sm" onclick="showModal('importModal')">+ Nhập kho</button>
      </div>
    </div>
    <div class="content fade-in">
      <c:if test="${param.success != null}"><div class="alert alert-success">✅ Nhập kho thành công!</div></c:if>

      <div class="grid-2">
        <!-- TỒN KHO HIỆN TẠI -->
        <div class="card" style="grid-column:span 2">
          <div class="card-header">
            <div class="card-title">📦 Tình trạng tồn kho</div>
            <input type="text" class="form-control" style="width:220px" placeholder="Tìm sản phẩm..." oninput="filterTable(this.value,'stbl')"/>
          </div>
          <div class="table-wrap">
            <table id="stbl">
              <thead><tr><th>Sản phẩm</th><th>Danh mục</th><th>Hãng</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
              <tbody>
                <c:forEach var="p" items="${products}">
                <tr>
                  <td><strong><c:out value="${p.productName}"/></strong><br/><small class="text-sub"><c:out value="${p.model}"/></small></td>
                  <td><c:out value="${p.catName}"/></td>
                  <td><c:out value="${p.brand}"/></td>
                  <td class="fw-700 text-primary"><fmt:formatNumber value="${p.price}" pattern="#,###"/>đ</td>
                  <td><strong>${p.stockQty}</strong></td>
                  <td>
                    <c:choose>
                      <c:when test="${p.stockQty == 0}"><span class="badge badge-nostock">Hết hàng</span></c:when>
                      <c:when test="${p.stockQty <= 5}"><span class="badge badge-lowstock">Sắp hết</span></c:when>
                      <c:otherwise><span class="badge badge-instock">Còn hàng</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <button class="btn btn-sm btn-primary btn-icon quick-import-btn" type="button"
                      data-product-id="${p.productId}">+ Nhập</button>
                  </td>
                </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- LỊCH SỬ NHẬP KHO -->
      <div class="card">
        <div class="card-header"><div class="card-title">📋 Lịch sử nhập kho gần đây</div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá nhập</th><th>Nhà cung cấp</th><th>Nhân viên</th><th>Ngày nhập</th></tr></thead>
            <tbody>
              <c:forEach var="imp" items="${imports}">
              <tr>
                <td><strong>${imp[0]}</strong></td>
                <td class="fw-700 text-success">+${imp[1]}</td>
                <td><fmt:formatNumber value="${imp[2]}" pattern="#,###"/>đ</td>
                <td>${imp[3]}</td>
                <td>${imp[4]}</td>
                <td style="font-size:0.8rem;color:var(--text-sub)">${imp[5]}</td>
              </tr>
              </c:forEach>
              <c:if test="${empty imports}"><tr><td colspan="6" style="text-align:center;color:var(--text-dim);padding:24px">Chưa có lịch sử nhập kho</td></tr></c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- NHẬP KHO MODAL -->
<div id="importModal" class="modal hidden">
  <div class="modal-box" style="max-width:440px">
    <div class="modal-header">
      <span class="modal-title">📥 Nhập kho hàng</span>
      <button class="modal-close" onclick="hideModal('importModal')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/staff/stock">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="modal-body">
        <div class="form-group">
          <label>Sản phẩm *</label>
          <select id="importProductId" name="productId" required>
            <option value="">-- Chọn sản phẩm --</option>
            <c:forEach var="p" items="${products}">
            <option value="${p.productId}"><c:out value="${p.productName}"/> (Tồn: ${p.stockQty})</option>
            </c:forEach>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Số lượng nhập *</label><input type="number" name="quantity" min="1" value="1" required/></div>
          <div class="form-group"><label>Đơn giá nhập (VNĐ)</label><input type="number" name="unitCost" min="0" value=""/></div>
        </div>
        <div class="form-group"><label>Nhà cung cấp</label><input type="text" name="supplier" placeholder="Tên nhà cung cấp..."/></div>
        <div class="form-group"><label>Ghi chú</label><textarea name="note" rows="2"></textarea></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" onclick="hideModal('importModal')">Hủy</button>
        <button type="submit" class="btn btn-primary">📥 Xác nhận nhập kho</button>
      </div>
    </form>
  </div>
</div>

<script>
document.querySelectorAll('.quick-import-btn').forEach((button) => {
  button.addEventListener('click', () => {
    document.getElementById('importProductId').value = button.dataset.productId;
    showModal('importModal');
  });
});
</script>
</body></html>
