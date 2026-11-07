<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Người dùng" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">👥 Quản lý Người dùng</div>
      <div class="topbar-right">
        <button class="btn btn-primary btn-sm" onclick="openAddUser()">+ Thêm người dùng</button>
      </div>
    </div>
    <div class="content fade-in">
      <c:if test="${param.success != null}"><div class="alert alert-success">✅ Thao tác thành công!</div></c:if>
      <div class="card">
        <div class="card-header">
          <div class="card-title">👥 Danh sách người dùng</div>
          <input type="text" class="form-control" style="width:220px" placeholder="Tìm kiếm..." oninput="filterTable(this.value,'utbl')"/>
        </div>
        <div class="table-wrap">
          <table id="utbl">
            <thead><tr><th>#</th><th>Họ tên</th><th>Tài khoản</th><th>Email / SĐT</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
            <tbody>
              <c:forEach var="u" items="${users}" varStatus="st">
              <tr data-id="${u.userId}" data-name="${fn:escapeXml(u.fullName)}" data-username="${fn:escapeXml(u.username)}"
                  data-email="${fn:escapeXml(u.email)}" data-phone="${fn:escapeXml(u.phone)}" data-address="${fn:escapeXml(u.address)}" data-role="${fn:escapeXml(u.role)}">
                <td class="text-sub">${st.index+1}</td>
                <td><strong><c:out value="${u.fullName}"/></strong></td>
                <td><code style="background:#f1f5f9;padding:2px 6px;border-radius:4px;font-size:0.8rem"><c:out value="${u.username}"/></code></td>
                <td><c:out value="${u.email}"/><br/><small class="text-sub"><c:out value="${u.phone}"/></small></td>
                <td><span class="badge badge-${fn:escapeXml(u.role)}"><c:out value="${u.role}"/></span></td>
                <td><c:if test="${u.active}"><span class="badge badge-active">Hoạt động</span></c:if><c:if test="${!u.active}"><span class="badge badge-inactive">Bị khóa</span></c:if></td>
                <td style="font-size:0.8rem;color:var(--text-sub)"><c:out value="${u.createdAt}"/></td>
                <td>
                  <div style="display:flex;gap:4px">
                    <button class="btn btn-sm btn-outline btn-icon user-edit-btn" type="button">✏️</button>
                    <form method="post" action="${pageContext.request.contextPath}/admin/users" style="display:inline">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
                      <input type="hidden" name="action" value="toggle"/>
                      <input type="hidden" name="userId" value="${u.userId}"/>
                      <button type="submit" class="btn btn-sm btn-icon ${u.active ? 'btn-warning' : 'btn-success'}">${u.active?'🔒':'🔓'}</button>
                    </form>
                  </div>
                </td>
              </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<div id="userModal" class="modal hidden">
  <div class="modal-box">
    <div class="modal-header">
      <span class="modal-title" id="userModalTitle">➕ Thêm người dùng</span>
      <button class="modal-close" onclick="hideModal('userModal')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/admin/users">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="modal-body">
        <input type="hidden" id="uAction" name="action" value="add"/>
        <input type="hidden" id="uId" name="userId"/>
        <div class="form-row">
          <div class="form-group"><label>Họ tên *</label><input type="text" id="uName" name="fullName" required/></div>
          <div class="form-group"><label>Username *</label><input type="text" id="uUsername" name="username" required/></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Mật khẩu</label><input type="password" id="uPass" name="password" placeholder="Để trống = giữ nguyên"/></div>
          <div class="form-group"><label>Vai trò</label>
            <select id="uRole" name="role">
              <option value="customer">Khách hàng</option>
              <option value="staff">Nhân viên</option>
              <option value="admin">Admin</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Email</label><input type="email" id="uEmail" name="email"/></div>
          <div class="form-group"><label>Số điện thoại</label><input type="text" id="uPhone" name="phone"/></div>
        </div>
        <div class="form-group"><label>Địa chỉ</label><input type="text" id="uAddress" name="address"/></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" onclick="hideModal('userModal')">Hủy</button>
        <button type="submit" class="btn btn-primary">💾 Lưu</button>
      </div>
    </form>
  </div>
</div>

<script>
function openAddUser(){
  ['userModalTitle','uAction','uId','uName','uUsername','uEmail','uPhone','uAddress'].forEach(id=>{ const el=document.getElementById(id); if(id==='userModalTitle') el.textContent='➕ Thêm người dùng'; else if(id==='uAction') el.value='add'; else el.value=''; });
  document.getElementById('uUsername').readOnly=false; document.getElementById('uPass').required=true;
  document.getElementById('uRole').value='customer'; showModal('userModal');
}
document.querySelectorAll('.user-edit-btn').forEach(btn=>{
  btn.addEventListener('click',()=>{
    const ds=btn.closest('tr').dataset;
    document.getElementById('userModalTitle').textContent='✏️ Sửa người dùng';
    document.getElementById('uAction').value='edit'; document.getElementById('uId').value=ds.id;
    document.getElementById('uName').value=ds.name; document.getElementById('uUsername').value=ds.username;
    document.getElementById('uEmail').value=ds.email; document.getElementById('uPhone').value=ds.phone;
    document.getElementById('uAddress').value=ds.address; document.getElementById('uRole').value=ds.role;
    document.getElementById('uUsername').readOnly=true; document.getElementById('uPass').required=false;
    document.getElementById('uPass').value=''; showModal('userModal');
  });
});
</script>
</body></html>
