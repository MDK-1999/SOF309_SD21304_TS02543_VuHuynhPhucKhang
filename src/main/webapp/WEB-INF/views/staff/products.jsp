<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Sản phẩm" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">💻 Quản lý Sản phẩm</div>
      <div class="topbar-right">
        <form action="${pageContext.request.contextPath}/staff/products" method="get" style="display:flex;gap:0">
          <div class="search-box">
            <input type="text" name="q" value="${fn:escapeXml(q)}" placeholder="Tìm sản phẩm..."/>
            <button type="submit">🔍</button>
          </div>
        </form>
        <button class="btn btn-primary btn-sm" onclick="showModal('addModal')">+ Thêm sản phẩm</button>
      </div>
    </div>
    <div class="content fade-in">
      <c:if test="${not empty sessionScope.flashType}"><div id="toast" class="toast ${fn:escapeXml(sessionScope.flashType)}"><div class="toast-icon">${sessionScope.flashType eq 'error' ? '❌' : '✅'}</div><div class="toast-body"><div class="toast-title"><c:out value="${sessionScope.flashTitle}"/></div><div class="toast-msg"><c:out value="${sessionScope.flashMsg}"/></div></div><button class="toast-close" onclick="closeToast()">✕</button><div class="toast-bar"></div></div></c:if>
      <c:remove var="flashType" scope="session"/><c:remove var="flashTitle" scope="session"/><c:remove var="flashMsg" scope="session"/>
      <c:if test="${param.success != null}"><div class="alert alert-success">✅ Cập nhật thành công!</div></c:if>

      <div class="card">
        <div class="card-header">
          <div class="card-title">💻 Danh sách sản phẩm (${products.size()})</div>
          <input type="text" class="form-control" style="width:220px" placeholder="Lọc nhanh..." oninput="filterTable(this.value,'ptbl')"/>
        </div>
        <div class="table-wrap">
          <table id="ptbl">
            <thead><tr><th>#</th><th>Tên sản phẩm</th><th>Danh mục</th><th>Hãng</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
            <tbody>
              <c:forEach var="p" items="${products}" varStatus="st">
              <tr data-id="${p.productId}" data-catid="${p.catId}" data-name="${fn:escapeXml(p.productName)}"
                  data-brand="${fn:escapeXml(p.brand)}" data-model="${fn:escapeXml(p.model)}" data-price="${p.price}"
                  data-specs="${fn:escapeXml(p.specs)}" data-desc="${fn:escapeXml(p.description)}" data-active="${p.active}">
                <td class="text-sub">${st.index+1}</td>
                <td><strong><c:out value="${p.productName}"/></strong><br/><small class="text-sub"><c:out value="${p.model}"/></small></td>
                <td><c:out value="${p.catName}"/></td>
                <td><span class="badge badge-blue"><c:out value="${p.brand}"/></span></td>
                <td class="fw-700 text-primary"><fmt:formatNumber value="${p.price}" pattern="#,###"/>đ</td>
                <td>
                  <c:choose>
                    <c:when test="${p.stockQty == 0}"><span class="badge badge-nostock">${p.stockQty} — Hết</span></c:when>
                    <c:when test="${p.stockQty <= 5}"><span class="badge badge-lowstock">${p.stockQty} — Sắp hết</span></c:when>
                    <c:otherwise><span class="badge badge-instock">${p.stockQty}</span></c:otherwise>
                  </c:choose>
                </td>
                <td><c:if test="${p.active}"><span class="badge badge-active">Đang bán</span></c:if><c:if test="${!p.active}"><span class="badge badge-inactive">Ngừng bán</span></c:if></td>
                <td>
                  <button class="btn btn-sm btn-outline btn-icon edit-product-btn" type="button">✏️</button>
                </td>
              </tr>
              </c:forEach>
              <c:if test="${empty products}"><tr><td colspan="8"><div class="empty-state"><div class="empty-icon">💻</div><p>Chưa có sản phẩm nào</p></div></td></tr></c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- ADD/EDIT MODAL -->
<div id="addModal" class="modal hidden">
  <div class="modal-box">
    <div class="modal-header">
      <span class="modal-title" id="modalTitle">➕ Thêm sản phẩm mới</span>
      <button class="modal-close" onclick="hideModal('addModal')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/staff/products">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="modal-body">
        <input type="hidden" id="fAction" name="action" value="add"/>
        <input type="hidden" id="fProductId" name="productId"/>
        <div class="form-row">
          <div class="form-group"><label>Tên sản phẩm *</label><input type="text" id="fName" name="productName" required/></div>
          <div class="form-group"><label>Danh mục *</label>
            <select id="fCat" name="catId" required>
              <option value="">-- Chọn danh mục --</option>
              <c:forEach var="c" items="${categories}"><option value="${c.catId}"><c:out value="${c.icon}"/> <c:out value="${c.catName}"/></option></c:forEach>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Hãng</label><input type="text" id="fBrand" name="brand"/></div>
          <div class="form-group"><label>Model</label><input type="text" id="fModel" name="model"/></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Giá bán (VNĐ) *</label><input type="number" id="fPrice" name="price" min="0" required/></div>
          <div class="form-group"><label>Tồn kho</label><input type="number" id="fStock" name="stockQty" min="0" value="0"/></div>
        </div>
        <div class="form-group"><label>Thông số kỹ thuật</label><textarea id="fSpecs" name="specs" rows="2"></textarea></div>
        <div class="form-group"><label>Mô tả</label><textarea id="fDesc" name="description" rows="2"></textarea></div>
        <div class="form-group" id="activeGroup" style="display:none">
          <label>Trạng thái</label>
          <select id="fActive" name="isActive">
            <option value="true">Đang bán</option>
            <option value="false">Ngừng bán</option>
          </select>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" onclick="hideModal('addModal')">Hủy</button>
        <button type="submit" class="btn btn-primary">💾 Lưu sản phẩm</button>
      </div>
    </form>
  </div>
</div>

<script>

document.querySelectorAll('.edit-product-btn').forEach(btn=>{
  btn.addEventListener('click',()=>{
    const row=btn.closest('tr'); const ds=row.dataset;
    document.getElementById('modalTitle').textContent='✏️ Sửa sản phẩm';
    document.getElementById('fAction').value='update';
    document.getElementById('fProductId').value=ds.id;
    document.getElementById('fName').value=ds.name;
    document.getElementById('fCat').value=ds.catid;
    document.getElementById('fBrand').value=ds.brand;
    document.getElementById('fModel').value=ds.model;
    document.getElementById('fPrice').value=ds.price;
    document.getElementById('fSpecs').value=ds.specs;
    document.getElementById('fDesc').value=ds.desc;
    document.getElementById('fActive').value=ds.active;
    document.getElementById('fStock').closest('.form-group').style.display='none';
    document.getElementById('activeGroup').style.display='block';
    showModal('addModal');
  });
});
function openAddModal(){
  document.getElementById('modalTitle').textContent='➕ Thêm sản phẩm mới';
  document.getElementById('fAction').value='add';
  document.getElementById('fProductId').value='';
  ['fName','fBrand','fModel','fSpecs','fDesc'].forEach(id=>document.getElementById(id).value='');
  document.getElementById('fPrice').value=''; document.getElementById('fStock').value='0';
  document.getElementById('fCat').value='';
  document.getElementById('fStock').closest('.form-group').style.display='';
  document.getElementById('activeGroup').style.display='none';
  showModal('addModal');
}
document.querySelector('.btn-primary.btn-sm').onclick=openAddModal;
</script>
</body></html>
