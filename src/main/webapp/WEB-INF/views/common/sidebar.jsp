<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  com.pcshop.model.User u = (com.pcshop.model.User) session.getAttribute("loginUser");
  String uri = request.getRequestURI();
  String ctx = request.getContextPath();
  String role = (u != null) ? u.getRole() : "";
  String initials = "US";
  if (u != null && u.getFullName() != null) {
    String[] p = u.getFullName().trim().split("\\s+");
    if (p.length >= 2) initials = (""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();
    else initials = u.getFullName().substring(0, Math.min(2, u.getFullName().length())).toUpperCase();
  }
  pageContext.setAttribute("sidebarInitials", initials);
  // Customer không dùng sidebar chung - chỉ admin/staff
  if ("customer".equals(role)) return;
%>
<div class="sidebar">
  <a href="<%= ctx %>/" class="sidebar-brand">
    <div class="brand-logo"><img class="brand-logo-image" src="<%= ctx %>/images/branding/gearastral-logo.png" alt="GearAstral" /></div>
  </a>

  <div class="sidebar-user">
    <div class="user-ava"><c:out value="${sidebarInitials}"/></div>
    <div>
      <div class="user-name"><c:out value="${sessionScope.loginUser.fullName}"/></div>
      <div class="user-role"><%= "admin".equals(role)?"Quản trị viên":"Nhân viên" %></div>
    </div>
  </div>

  <nav class="sidebar-nav">
    <div class="nav-section">Tổng quan</div>
    <a href="<%= ctx %>/dashboard" class="nav-item <%= uri.contains("/dashboard") ? "active" : "" %>">
      <span class="ni">📊</span> Dashboard
    </a>

    <div class="nav-section">Quản lý</div>
    <a href="<%= ctx %>/staff/products" class="nav-item <%= uri.contains("/products") ? "active" : "" %>">
      <span class="ni">💻</span> Sản phẩm
    </a>
    <a href="<%= ctx %>/staff/orders" class="nav-item <%= uri.contains("/orders") ? "active" : "" %>">
      <span class="ni">📦</span> Đơn hàng
    </a>
    <a href="<%= ctx %>/staff/stock" class="nav-item <%= uri.contains("/stock") ? "active" : "" %>">
      <span class="ni">🏪</span> Nhập kho
    </a>

    <% if ("admin".equals(role)) { %>
    <div class="nav-section">Admin</div>
    <a href="<%= ctx %>/admin/users" class="nav-item <%= uri.contains("/admin/users") ? "active" : "" %>">
      <span class="ni">👥</span> Người dùng
    </a>
    <a href="<%= ctx %>/admin/categories" class="nav-item <%= uri.contains("/admin/categories") ? "active" : "" %>">
      <span class="ni">📂</span> Danh mục
    </a>
    <a href="<%= ctx %>/admin/report" class="nav-item <%= uri.contains("/admin/report") ? "active" : "" %>">
      <span class="ni">📈</span> Báo cáo
    </a>
    <% } %>
  </nav>

  <div class="sidebar-footer">
    <form method="post" action="<%= ctx %>/logout" class="logout-form">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <button type="submit" class="logout-btn">Đăng xuất</button>
    </form>
  </div>
</div>
