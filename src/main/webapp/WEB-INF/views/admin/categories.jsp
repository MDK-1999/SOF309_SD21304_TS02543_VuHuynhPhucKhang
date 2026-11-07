<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Danh mục" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">📂 Quản lý Danh mục</div>
      <div class="topbar-right"><button class="btn btn-primary btn-sm" onclick="openAdd()">+ Thêm danh mục</button></div>
    </div>
    <div class="content fade-in">
      <c:if test="${param.success != null}"><div class="alert alert-success">✅ Thao tác thành công!</div></c:if>
      <div class="card">
        <div class="card-header"><div class="card-title">📂 Danh sách danh mục</div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>#</th><th>Icon</th><th>Tên danh mục</th><th>Mô tả</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              <c:forEach var="c" items="${categories}" varStatus="st">
              <tr data-id="${c.catId}" data-name="${fn:escapeXml(c.catName)}" data-desc="${fn:escapeXml(c.description)}" data-icon="${fn:escapeXml(c.icon)}" data-active="${c.active}">
                <td class="text-sub">${st.index+1}</td>
                <td style="font-size:1.4rem;text-align:center"><c:out value="${c.icon}"/></td>
                <td><strong><c:out value="${c.catName}"/></strong></td>
                <td class="text-sub"><c:out value="${c.description}"/></td>
                <td><c:if test="${c.active}"><span class="badge badge-active">Hoạt động</span></c:if><c:if test="${!c.active}"><span class="badge badge-inactive">Ẩn</span></c:if></td>
                <td><button class="btn btn-sm btn-outline btn-icon cat-edit-btn" type="button">✏️ Sửa</button></td>
              </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
<div id="catModal" class="modal hidden">
  <div class="modal-box" style="max-width:440px">
    <div class="modal-header"><span class="modal-title" id="catModalTitle">➕ Thêm danh mục</span><button class="modal-close" onclick="hideModal('catModal')">✕</button></div>
    <form method="post" action="${pageContext.request.contextPath}/admin/categories">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="modal-body">
        <input type="hidden" id="cAction" name="action" value="add"/>
        <input type="hidden" id="cId" name="catId"/>
        <div class="form-row">
          <div class="form-group"><label>Icon (emoji)</label><input type="text" id="cIcon" name="icon" value="📦" maxlength="4" style="font-size:1.3rem;text-align:center"/></div>
          <div class="form-group"><label>Tên danh mục *</label><input type="text" id="cName" name="catName" required/></div>
        </div>
        <div class="form-group"><label>Mô tả</label><textarea id="cDesc" name="description" rows="2"></textarea></div>
        <div class="form-group" id="cActiveGroup" style="display:none">
          <label>Trạng thái</label>
          <select id="cActive" name="isActive"><option value="true">Hoạt động</option><option value="false">Ẩn</option></select>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" onclick="hideModal('catModal')">Hủy</button>
        <button type="submit" class="btn btn-primary">💾 Lưu</button>
      </div>
    </form>
  </div>
</div>
<script>
function openAdd(){
  document.getElementById('catModalTitle').textContent='➕ Thêm danh mục';
  document.getElementById('cAction').value='add'; document.getElementById('cId').value='';
  document.getElementById('cName').value=''; document.getElementById('cDesc').value=''; document.getElementById('cIcon').value='📦';
  document.getElementById('cActiveGroup').style.display='none'; showModal('catModal');
}
document.querySelectorAll('.cat-edit-btn').forEach(btn=>{
  btn.addEventListener('click',()=>{
    const ds=btn.closest('tr').dataset;
    document.getElementById('catModalTitle').textContent='✏️ Sửa danh mục';
    document.getElementById('cAction').value='update'; document.getElementById('cId').value=ds.id;
    document.getElementById('cName').value=ds.name; document.getElementById('cDesc').value=ds.desc;
    document.getElementById('cIcon').value=ds.icon; document.getElementById('cActive').value=ds.active;
    document.getElementById('cActiveGroup').style.display=''; showModal('catModal');
  });
});
</script>
</body></html>
