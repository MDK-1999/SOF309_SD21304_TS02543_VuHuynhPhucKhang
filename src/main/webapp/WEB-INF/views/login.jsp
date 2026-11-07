<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<% request.setAttribute("pageTitle", "GearAstral - Đăng nhập"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=7.1.3" />
</head>
<body class="auth-page">
<%
  String fType=(String)session.getAttribute("flashType"), fTitle=(String)session.getAttribute("flashTitle"), fMsg=(String)session.getAttribute("flashMsg");
  session.removeAttribute("flashType"); session.removeAttribute("flashTitle"); session.removeAttribute("flashMsg");
  if(fType==null && "registered".equals(request.getParameter("success"))){ fType="success"; fTitle="Đăng ký thành công"; fMsg="Tài khoản đã được tạo. Bạn có thể đăng nhập ngay."; }
  if(fType==null && "session".equals(request.getParameter("error"))){ fType="warning"; fTitle="Phiên đã hết hạn"; fMsg="Vui lòng đăng nhập lại để tiếp tục."; }
  String icon = "error".equals(fType)?"!":"warning".equals(fType)?"!":"✓";
  pageContext.setAttribute("toastType", fType); pageContext.setAttribute("toastTitle", fTitle);
  pageContext.setAttribute("toastMessage", fMsg); pageContext.setAttribute("toastIcon", icon);
%>
<c:if test="${not empty toastType}">
<div id="toast" class="toast ${fn:escapeXml(toastType)}" role="status" aria-live="polite">
  <div class="toast-icon" aria-hidden="true"><c:out value="${toastIcon}"/></div>
  <div class="toast-body"><div class="toast-title"><c:out value="${toastTitle}"/></div><div class="toast-msg"><c:out value="${toastMessage}"/></div></div>
  <button type="button" class="toast-close" onclick="closeToast()" aria-label="Đóng thông báo">×</button>
  <div class="toast-bar"></div>
</div>
</c:if>

<main class="login-wrap">
  <a href="${pageContext.request.contextPath}/" class="back-link">← Trang chủ</a>
  <div class="login-logo">
    <span class="login-logo-surface"><img class="login-logo-image" src="${pageContext.request.contextPath}/images/branding/gearastral-logo-light.png" alt="GearAstral" /></span>
    <p>Build chuẩn, nâng cấp dễ</p>
  </div>
  <section class="login-card" aria-labelledby="loginTitle">
    <h2 id="loginTitle">Đăng nhập hệ thống</h2>
    <form method="post" action="${pageContext.request.contextPath}/login">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="form-group">
        <label for="loginUsername">Tên đăng nhập</label>
        <input id="loginUsername" type="text" name="username" placeholder="Nhập tên đăng nhập" autocomplete="username" minlength="4" maxlength="32" pattern="[A-Za-z0-9._-]{4,32}" autofocus required />
      </div>
      <div class="form-group">
        <label for="loginPassword">Mật khẩu</label>
        <input id="loginPassword" type="password" name="password" placeholder="Nhập mật khẩu" autocomplete="current-password" maxlength="72" required />
      </div>
      <button type="submit" class="btn-login">Đăng nhập</button>
    </form>
    <div class="login-links">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></div>
  </section>
</main>
</body></html>
