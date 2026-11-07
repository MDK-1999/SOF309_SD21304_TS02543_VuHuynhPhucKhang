<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%  com.pcshop.model.User u = (com.pcshop.model.User) session.getAttribute("loginUser");
    String ctx = request.getContextPath();
    String initials = "KH";
    if (u.getFullName() != null) {
        String[] p = u.getFullName().trim().split("\\s+");
        if (p.length >= 2) initials = (""+p[0].charAt(0)+p[p.length-1].charAt(0)).toUpperCase();
        else initials = u.getFullName().substring(0, Math.min(2, u.getFullName().length())).toUpperCase();
    }
    pageContext.setAttribute("customerInitials", initials);
%>
<c:set var="pageTitle" value="GearAstral - Tài khoản của tôi" />
<!DOCTYPE html>
<html lang="vi">
<head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head>
<body>

<!-- TOPBAR -->
<div style="position:fixed;top:0;left:0;right:0;height:62px;background:#fff;border-bottom:1px solid var(--border);
            display:flex;align-items:center;justify-content:space-between;padding:0 28px;z-index:100;
            box-shadow:0 1px 6px rgba(0,0,0,0.06)">
  <div style="display:flex;align-items:center;gap:12px">
    <div style="width:38px;height:38px;background:var(--primary);border-radius:6px;
                display:flex;align-items:center;justify-content:center;
                font-family:var(--font-head);font-weight:800;color:#fff;font-size:0.95rem">PC</div>
    <span style="font-family:var(--font-head);font-weight:800;font-size:1.05rem;color:var(--text-main)">
      PC<span style="color:var(--primary)">SHOP</span> Pro
    </span>
  </div>
  <div style="display:flex;align-items:center;gap:12px">
    <span style="font-size:0.85rem;color:var(--text-sub)">Xin chào, <strong><c:out value="${sessionScope.loginUser.fullName}"/></strong></span>
    <span class="badge badge-customer">Khách hàng</span>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="logout-form">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <button type="submit" class="btn btn-danger">Đăng xuất</button>
    </form>
  </div>
</div>

<div style="padding:84px 32px 32px;max-width:1100px;margin:0 auto">
  <%-- TOAST đăng nhập --%>
  <c:if test="${not empty sessionScope.flashLogin}">
  <div id="toast" class="toast success">
    <div class="toast-icon">✅</div>
    <div class="toast-body"><div class="toast-title">Đăng nhập thành công!</div><div class="toast-msg"><c:out value="${sessionScope.flashLogin}"/></div></div>
    <button class="toast-close" onclick="closeToast()">✕</button>
    <div class="toast-bar"></div>
  </div>
  </c:if>
  <c:remove var="flashLogin" scope="session"/>

  <!-- THÔNG TIN TÀI KHOẢN -->
  <div style="display:grid;grid-template-columns:280px 1fr;gap:24px;margin-bottom:28px">
    <div class="card" style="margin:0">
      <div class="card-body" style="text-align:center;padding:28px">
        <div style="width:72px;height:72px;background:var(--primary);border-radius:50%;
                    display:flex;align-items:center;justify-content:center;
                    font-family:var(--font-head);font-weight:800;font-size:1.6rem;
                    color:#fff;margin:0 auto 16px"><c:out value="${customerInitials}"/></div>
        <div style="font-family:var(--font-head);font-size:1.1rem;font-weight:700;margin-bottom:4px"><c:out value="${sessionScope.loginUser.fullName}"/></div>
        <div style="font-size:0.82rem;color:var(--text-sub);margin-bottom:12px">@<c:out value="${sessionScope.loginUser.username}"/></div>
        <span class="badge badge-customer" style="font-size:0.75rem">Khách hàng</span>
        <div style="margin-top:20px;text-align:left;font-size:0.85rem">
          <div style="padding:8px 0;border-bottom:1px solid var(--border);color:var(--text-sub)">
            📧 <c:out value="${empty sessionScope.loginUser.email ? 'Chưa cập nhật' : sessionScope.loginUser.email}"/>
          </div>
          <div style="padding:8px 0;border-bottom:1px solid var(--border);color:var(--text-sub)">
            📞 <c:out value="${empty sessionScope.loginUser.phone ? 'Chưa cập nhật' : sessionScope.loginUser.phone}"/>
          </div>
          <div style="padding:8px 0;color:var(--text-sub)">
            📍 <c:out value="${empty sessionScope.loginUser.address ? 'Chưa cập nhật' : sessionScope.loginUser.address}"/>
          </div>
        </div>
      </div>
    </div>

    <!-- THỐNG KÊ NHANH -->
    <div>
      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:16px;margin-bottom:16px">
        <div class="stat-card" style="margin:0">
          <div class="stat-icon-box ib-blue">📦</div>
          <div class="stat-card-body">
            <div class="stat-value">${myOrders.size()}</div>
            <div class="stat-label">Tổng đơn hàng</div>
          </div>
        </div>
        <div class="stat-card" style="margin:0">
          <div class="stat-icon-box ib-orange">⏳</div>
          <div class="stat-card-body">
            <div class="stat-value">
              <c:set var="pendCount" value="0"/>
              <c:forEach var="o" items="${myOrders}">
                <c:if test="${o.status == 'pending' || o.status == 'confirmed' || o.status == 'processing'}">
                  <c:set var="pendCount" value="${pendCount + 1}"/>
                </c:if>
              </c:forEach>
              ${pendCount}
            </div>
            <div class="stat-label">Đang xử lý</div>
          </div>
        </div>
        <div class="stat-card" style="margin:0">
          <div class="stat-icon-box ib-green">✅</div>
          <div class="stat-card-body">
            <div class="stat-value">
              <c:set var="doneCount" value="0"/>
              <c:forEach var="o" items="${myOrders}">
                <c:if test="${o.status == 'done'}"><c:set var="doneCount" value="${doneCount + 1}"/></c:if>
              </c:forEach>
              ${doneCount}
            </div>
            <div class="stat-label">Đã hoàn thành</div>
          </div>
        </div>
      </div>

      <!-- HƯỚNG DẪN -->
      <div style="background:var(--blue-l);border:1px solid #bfdbfe;border-radius:8px;padding:16px 20px">
        <div style="font-weight:700;color:var(--primary);margin-bottom:10px;font-size:0.9rem">💡 Muốn mua PC hoặc linh kiện?</div>
        <div style="font-size:0.85rem;color:var(--text-sub);line-height:1.7">
          Liên hệ nhân viên GearAstral qua: <br/>
          📞 <strong style="color:var(--text-main)">0901 552 900</strong> &nbsp;|&nbsp;
          📧 <strong style="color:var(--text-main)">contact@gearastral.vn</strong><br/>
          Nhân viên sẽ tư vấn và tạo đơn hàng cho bạn ngay.
        </div>
        <div style="margin-top:12px;display:flex;gap:10px">
          <a href="#products-section" class="btn btn-primary btn-sm">💻 Xem sản phẩm</a>
          <a href="#builds-section" class="btn btn-outline btn-sm">🔧 Xem Build PC</a>
        </div>
      </div>
    </div>
  </div>

  <!-- ĐƠN HÀNG CỦA TÔI -->
  <div class="card mb-6">
    <div class="card-header">
      <div class="card-title">📦 Đơn hàng của tôi</div>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr><th>#</th><th>Ngày đặt</th><th>Loại</th><th>Tổng tiền</th><th>TT đơn hàng</th><th>TT thanh toán</th></tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty myOrders}">
              <tr><td colspan="6">
                <div class="empty-state">
                  <div class="empty-icon">📦</div>
                  <p style="font-size:0.95rem;font-weight:600;margin-bottom:6px">Bạn chưa có đơn hàng nào</p>
                  <p style="color:var(--text-dim);font-size:0.85rem">Liên hệ nhân viên để đặt mua PC hoặc linh kiện</p>
                </div>
              </td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="o" items="${myOrders}">
              <tr>
                <td class="fw-700">#${o.orderId}</td>
                <td style="font-size:0.82rem;color:var(--text-sub)">${o.createdAt}</td>
                <td>
                  <span class="badge ${o.orderType == 'build' ? 'badge-purple' : 'badge-blue'}">
                    ${o.orderType == 'build' ? '🔧 Build PC' : '💻 Sản phẩm'}
                  </span>
                </td>
                <td class="fw-700 text-primary"><fmt:formatNumber value="${o.finalAmount}" pattern="#,###"/>đ</td>
                <td>
                  <span class="badge badge-${o.status}">
                    <c:choose>
                      <c:when test="${o.status == 'pending'}">⏳ Chờ xử lý</c:when>
                      <c:when test="${o.status == 'confirmed'}">✅ Đã xác nhận</c:when>
                      <c:when test="${o.status == 'processing'}">🔧 Đang xử lý</c:when>
                      <c:when test="${o.status == 'shipping'}">🚚 Đang giao</c:when>
                      <c:when test="${o.status == 'done'}">✔️ Hoàn thành</c:when>
                      <c:when test="${o.status == 'cancelled'}">❌ Đã hủy</c:when>
                      <c:otherwise>${o.status}</c:otherwise>
                    </c:choose>
                  </span>
                </td>
                <td>
                  <span class="badge badge-${o.paymentStatus}">
                    <c:choose>
                      <c:when test="${o.paymentStatus == 'paid'}">✅ Đã thanh toán</c:when>
                      <c:when test="${o.paymentStatus == 'unpaid'}">💳 Chưa thanh toán</c:when>
                      <c:otherwise>${o.paymentStatus}</c:otherwise>
                    </c:choose>
                  </span>
                </td>
              </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>

  <!-- SẢN PHẨM NỔI BẬT -->
  <div id="products-section" style="margin-bottom:28px">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
      <h2 style="font-family:var(--font-head);font-size:1.1rem;font-weight:700">💻 Sản phẩm nổi bật</h2>
    </div>
    <div class="product-grid">
      <c:forEach var="p" items="${products}" begin="0" end="7">
      <div class="product-card">
        <div class="product-card-img"><img src="${pageContext.request.contextPath}/product-image?id=${p.productId}" alt="${fn:escapeXml(p.productName)}" width="240" height="140" loading="lazy" decoding="async" /></div>
        <div class="product-card-body">
          <a class="product-card-name" href="${pageContext.request.contextPath}/product?id=${p.productId}" style="display:block;color:inherit;text-decoration:none"><c:out value="${p.productName}"/></a>
          <div class="product-card-brand">${p.brand} | ${p.catName}</div>
          <div class="product-card-price"><fmt:formatNumber value="${p.price}" pattern="#,###"/>đ</div>
          <div class="product-card-stock">
            <c:choose>
              <c:when test="${p.stockQty == 0}"><span class="badge badge-nostock">Hết hàng</span></c:when>
              <c:when test="${p.stockQty <= 5}"><span class="badge badge-lowstock">Sắp hết (${p.stockQty})</span></c:when>
              <c:otherwise><span class="badge badge-instock">Còn hàng (${p.stockQty})</span></c:otherwise>
            </c:choose>
          </div>
          <div style="margin-top:10px;font-size:0.75rem;color:var(--text-sub)">${p.specs}</div>
        </div>
      </div>
      </c:forEach>
    </div>
  </div>

  <!-- BUILD PC -->
  <div id="builds-section">
    <div style="margin-bottom:16px">
      <h2 style="font-family:var(--font-head);font-size:1.1rem;font-weight:700">🔧 Bộ Build PC gợi ý</h2>
    </div>
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:16px">
      <c:forEach var="b" items="${builds}">
      <c:if test="${b.active}">
      <div class="build-card">
        <c:if test="${b.featured}"><div class="build-featured-tag">⭐ Nổi bật</div></c:if>
        <div class="build-usecase-tag"
          style="<c:choose>
            <c:when test='${b.useCase==\"Gaming\"}'>background:#fef9c3;color:#854d0e</c:when>
            <c:when test='${b.useCase==\"Đồ họa\"}'>background:#ede9fe;color:#6d28d9</c:when>
            <c:when test='${b.useCase==\"Văn phòng\"}'>background:#dbeafe;color:#1d4ed8</c:when>
            <c:otherwise>background:#f1f5f9;color:#475569</c:otherwise>
          </c:choose>">
          ${b.useCase}
        </div>
        <div style="font-family:var(--font-head);font-weight:700;font-size:1rem;margin-bottom:6px">${b.buildName}</div>
        <div style="font-size:0.8rem;color:var(--text-sub);margin-bottom:12px;line-height:1.5">${b.description}</div>
        <div style="font-family:var(--font-head);font-size:1.3rem;font-weight:800;color:var(--primary)">
          <fmt:formatNumber value="${b.totalPrice}" pattern="#,###"/>đ
        </div>
        <div style="margin-top:12px;font-size:0.8rem;color:var(--text-sub)">
          📞 Liên hệ để đặt hàng: <strong style="color:var(--primary)">0901 552 900</strong>
        </div>
      </div>
      </c:if>
      </c:forEach>
      <c:if test="${empty builds}">
        <div class="empty-state"><div class="empty-icon">🔧</div><p>Chưa có build PC nào</p></div>
      </c:if>
    </div>
  </div>

</div>

</body>
</html>
