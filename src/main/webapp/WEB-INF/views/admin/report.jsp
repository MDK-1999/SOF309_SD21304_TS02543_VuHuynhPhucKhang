<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="GearAstral Manager - Báo cáo" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar"><div class="topbar-title">📈 Báo cáo Doanh thu</div></div>
    <div class="content fade-in">
      <div class="stats-grid">
        <div class="stat-card"><div class="stat-icon-box ib-green">💰</div><div class="stat-card-body"><div class="stat-value" style="font-size:1.3rem"><fmt:formatNumber value="${todayRevenue}" pattern="#,###"/>đ</div><div class="stat-label">Hôm nay</div></div></div>
        <div class="stat-card"><div class="stat-icon-box ib-purple">📅</div><div class="stat-card-body"><div class="stat-value" style="font-size:1.3rem"><fmt:formatNumber value="${monthRevenue}" pattern="#,###"/>đ</div><div class="stat-label">Tháng này</div></div></div>
        <div class="stat-card"><div class="stat-icon-box ib-orange">📦</div><div class="stat-card-body"><div class="stat-value">${totalOrders}</div><div class="stat-label">Tổng đơn hàng</div></div></div>
        <div class="stat-card"><div class="stat-icon-box ib-blue">👥</div><div class="stat-card-body"><div class="stat-value">${totalCustomers}</div><div class="stat-label">Khách hàng</div></div></div>
      </div>
      <div class="card mb-6">
        <div class="card-header"><div class="card-title">📊 Doanh thu 7 ngày gần đây</div></div>
        <div class="card-body">
          <c:forEach var="row" items="${weeklyRevenue}">
          <div class="chart-row">
            <div class="chart-label">${row[0]}</div>
            <div class="chart-track"><div class="chart-fill" style="width:${row[1]>0?'60':'2'}%">${row[1]} đơn</div></div>
            <div class="chart-val"><fmt:formatNumber value="${row[2]}" pattern="#,###"/>đ</div>
          </div>
          </c:forEach>
          <c:if test="${empty weeklyRevenue}"><div class="empty-state"><div class="empty-icon">📊</div><p>Chưa có dữ liệu</p></div></c:if>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><div class="card-title">🧾 Đơn hàng gần đây</div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>#</th><th>Khách hàng</th><th>Tổng tiền</th><th>TT đơn</th><th>TT thanh toán</th><th>Ngày tạo</th></tr></thead>
            <tbody>
              <c:forEach var="o" items="${recentOrders}">
              <tr>
                <td class="fw-700">#${o.orderId}</td>
                <td>${o.customerName}<br/><small class="text-sub">${o.customerPhone}</small></td>
                <td class="fw-700 text-primary"><fmt:formatNumber value="${o.finalAmount}" pattern="#,###"/>đ</td>
                <td><span class="badge badge-${o.status}">${o.status}</span></td>
                <td><span class="badge badge-${o.paymentStatus}">${o.paymentStatus}</span></td>
                <td style="font-size:0.8rem;color:var(--text-sub)">${o.createdAt}</td>
              </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
</body></html>
