<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Dashboard" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">📊 Dashboard</div>
      <div class="topbar-right">
        <span class="topbar-time" id="timeDisplay"></span>
        <span class="topbar-badge badge-${fn:escapeXml(sessionScope.loginUser.role)}"><c:out value="${sessionScope.loginUser.role}"/></span>
      </div>
    </div>
    <div class="content fade-in">
      <%-- TOAST --%>
      <c:if test="${not empty sessionScope.flashLogin}"><div id="toast" class="toast success"><div class="toast-icon">✅</div><div class="toast-body"><div class="toast-title">Đăng nhập thành công!</div><div class="toast-msg"><c:out value="${sessionScope.flashLogin}"/></div></div><button class="toast-close" onclick="closeToast()">✕</button><div class="toast-bar"></div></div></c:if>
      <c:remove var="flashLogin" scope="session"/>
      <c:if test="${param.error=='forbidden'}"><div class="alert alert-danger">⛔ Bạn không có quyền truy cập.</div></c:if>

      <!-- STAT CARDS -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon-box ib-blue">💻</div>
          <div class="stat-card-body">
            <div class="stat-value">${totalProducts}</div>
            <div class="stat-label">Sản phẩm</div>
            <span class="stat-badge" style="background:#fee2e2;color:#991b1b">${lowStock} sắp hết</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-box ib-orange">📦</div>
          <div class="stat-card-body">
            <div class="stat-value">${totalOrders}</div>
            <div class="stat-label">Tổng đơn hàng</div>
            <span class="stat-badge badge-pending">${pendingOrders} chờ xử lý</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-box ib-green">💰</div>
          <div class="stat-card-body">
            <div class="stat-value" style="font-size:1.3rem"><fmt:formatNumber value="${todayRevenue}" pattern="#,###"/>đ</div>
            <div class="stat-label">Doanh thu hôm nay</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-box ib-purple">📅</div>
          <div class="stat-card-body">
            <div class="stat-value" style="font-size:1.3rem"><fmt:formatNumber value="${monthRevenue}" pattern="#,###"/>đ</div>
            <div class="stat-label">Doanh thu tháng này</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-box ib-blue">👥</div>
          <div class="stat-card-body">
            <div class="stat-value">${totalCustomers}</div>
            <div class="stat-label">Khách hàng</div>
          </div>
        </div>
      </div>

      <div class="grid-2">
        <!-- DOANH THU 7 NGÀY -->
        <div class="card">
          <div class="card-header"><div class="card-title">📈 Doanh thu 7 ngày</div></div>
          <div class="card-body">
            <c:choose>
              <c:when test="${empty weeklyRevenue}">
                <div class="empty-state"><div class="empty-icon">📊</div><p>Chưa có dữ liệu</p></div>
              </c:when>
              <c:otherwise>
                <c:forEach var="row" items="${weeklyRevenue}">
                <div class="chart-row">
                  <div class="chart-label">${row[0]}</div>
                  <div class="chart-track">
                    <div class="chart-fill" style="width:${row[2] == 0 ? '2' : '50'}%">
                      <c:if test="${row[1] > 0}">${row[1]} đơn</c:if>
                    </div>
                  </div>
                  <div class="chart-val"><fmt:formatNumber value="${row[2]}" pattern="#,###"/>đ</div>
                </div>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </div>
        </div>

        <!-- ĐƠN HÀNG GẦN ĐÂY -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">🛒 Đơn hàng gần đây</div>
            <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-outline btn-sm">Xem tất cả</a>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th></tr></thead>
              <tbody>
                <c:forEach var="o" items="${recentOrders}">
                <tr>
                  <td><strong>${o.customerName}</strong><br/><small class="text-sub">${o.customerPhone}</small></td>
                  <td class="fw-700 text-primary"><fmt:formatNumber value="${o.finalAmount}" pattern="#,###"/>đ</td>
                  <td><span class="badge badge-${o.status}">${o.status}</span></td>
                </tr>
                </c:forEach>
                <c:if test="${empty recentOrders}"><tr><td colspan="3" style="text-align:center;color:var(--text-dim);padding:24px">Chưa có đơn hàng</td></tr></c:if>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- QUICK ACTIONS -->
      <div class="card">
        <div class="card-header"><div class="card-title">⚡ Thao tác nhanh</div></div>
        <div class="card-body" style="display:flex;gap:12px;flex-wrap:wrap">
          <a href="${pageContext.request.contextPath}/staff/orders" class="btn btn-primary">📦 Tạo đơn hàng</a>
          <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-outline">💻 Quản lý sản phẩm</a>
          <a href="${pageContext.request.contextPath}/staff/stock" class="btn btn-outline">🏪 Nhập kho</a>
          <c:if test="${sessionScope.loginUser.admin}">
            <a href="${pageContext.request.contextPath}/admin/report" class="btn btn-ghost">📈 Báo cáo</a>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</div>
<script>
</script>
</body></html>
