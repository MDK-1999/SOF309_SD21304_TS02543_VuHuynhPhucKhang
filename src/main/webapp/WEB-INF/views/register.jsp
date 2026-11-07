<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% request.setAttribute("pageTitle", "GearAstral - Đăng ký"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=7.1.3" />
</head>
<body class="auth-page">
<main class="reg-wrap">
  <a href="${pageContext.request.contextPath}/login" class="back-link">← Quay lại đăng nhập</a>
  <div class="login-logo">
    <span class="login-logo-surface"><img class="login-logo-image" src="${pageContext.request.contextPath}/images/branding/gearastral-logo-light.png" alt="GearAstral" /></span>
  </div>
  <section class="reg-card" aria-labelledby="registerTitle">
    <h2 id="registerTitle">Tạo tài khoản mới</h2>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}" /></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/register">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <div class="form-row">
        <div class="form-group"><label for="fullName">Họ tên</label><input id="fullName" type="text" name="fullName" value="<c:out value='${fullName}'/>" autocomplete="name" minlength="2" maxlength="100" required /></div>
        <div class="form-group"><label for="username">Tên đăng nhập</label><input id="username" type="text" name="username" value="<c:out value='${username}'/>" autocomplete="username" minlength="4" maxlength="32" pattern="[A-Za-z0-9._\-]{4,32}" required /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label for="password">Mật khẩu</label><input id="password" type="password" name="password" autocomplete="new-password" minlength="8" maxlength="72" required /><small>Tối thiểu 8 ký tự, có chữ hoa, chữ thường và chữ số.</small></div>
        <div class="form-group"><label for="confirm">Xác nhận mật khẩu</label><input id="confirm" type="password" name="confirm" autocomplete="new-password" maxlength="72" required /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label for="email">Email</label><input id="email" type="email" name="email" value="<c:out value='${email}'/>" autocomplete="email" maxlength="100" /></div>
        <div class="form-group"><label for="phone">Số điện thoại</label><input id="phone" type="tel" name="phone" value="<c:out value='${phone}'/>" autocomplete="tel" inputmode="tel" maxlength="15" placeholder="0901234567" required /></div>
      </div>
      <div class="form-group"><label for="address">Địa chỉ</label><input id="address" type="text" name="address" value="<c:out value='${address}'/>" autocomplete="street-address" maxlength="200" /></div>
      <button type="submit" class="btn-reg">Đăng ký ngay</button>
    </form>
    <div class="reg-links">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></div>
  </section>
</main>
</body></html>
