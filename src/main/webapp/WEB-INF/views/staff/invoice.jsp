<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Xuất bill #${order.orderId} - GearAstral" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/shop.css?v=7.1.0" />
  <style>
    @media print {
      .sidebar,.topbar,.print-actions { display:none !important; }
      .app-wrap,.main,.content { display:block !important; margin:0 !important; padding:0 !important; width:100% !important; }
      .invoice-page { border:0 !important; margin:0 !important; width:100% !important; padding:0 !important; }
      body { background:#fff !important; }
    }
  </style>
</head>
<body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">Xuất bill đơn hàng #${order.orderId}</div>
      <div class="topbar-right">
        <a class="btn btn-outline btn-sm" href="${pageContext.request.contextPath}/staff/orders">Quay lại đơn hàng</a>
        <button class="btn btn-primary btn-sm" type="button" onclick="window.print()">In / Lưu PDF</button>
      </div>
    </div>
    <div class="content fade-in">
      <main class="invoice-page">
        <div class="invoice-head">
          <div>
            <h1 class="invoice-title">HÓA ĐƠN BÁN HÀNG</h1>
            <p><strong>GearAstral</strong></p>
            <p>110 Trần Thánh Tông, Phường Tân Sơn, TP Hồ Chí Minh</p>
            <p>Hotline: 0901 552 900 | Email: contact@gearastral.vn</p>
          </div>
          <div class="invoice-meta">
            <p><strong>Mã hóa đơn:</strong> INV-${order.orderId}</p>
            <p><strong>Mã đơn:</strong> #${order.orderId}</p>
            <p><strong>Ngày đặt:</strong> ${order.createdAt}</p>
            <p><strong>Trạng thái:</strong> ${order.status}</p>
          </div>
        </div>

        <div class="invoice-info">
          <div class="invoice-box">
            <h3>Thông tin khách hàng</h3>
            <p><strong>Họ tên:</strong> ${order.customerName}</p>
            <p><strong>SĐT:</strong> ${order.customerPhone}</p>
            <p><strong>Email:</strong> ${order.customerEmail}</p>
            <p><strong>Địa chỉ:</strong> ${order.shippingAddr}</p>
          </div>
          <div class="invoice-box">
            <h3>Thanh toán</h3>
            <p><strong>Phương thức:</strong> <c:choose><c:when test="${order.paymentMethod == 'qr'}">Thanh toán QR</c:when><c:otherwise>COD</c:otherwise></c:choose></p>
            <p><strong>Trạng thái:</strong> ${order.paymentStatus}</p>
            <p><strong>Ghi chú:</strong> ${order.notes}</p>
          </div>
        </div>

        <table class="table">
          <thead><tr><th>STT</th><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>
          <tbody>
            <c:forEach var="item" items="${order.items}" varStatus="st">
              <tr>
                <td>${st.index + 1}</td>
                <td><strong>${item.productName}</strong></td>
                <td>${item.quantity}</td>
                <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" />đ</td>
                <td><strong><fmt:formatNumber value="${item.subtotal}" type="number" groupingUsed="true" />đ</strong></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>

        <div class="invoice-total">
          <div><span>Tạm tính</span><strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" />đ</strong></div>
          <div><span>Giảm giá</span><strong><fmt:formatNumber value="${order.discount}" type="number" groupingUsed="true" />đ</strong></div>
          <div class="grand"><span>Tổng thanh toán</span><span><fmt:formatNumber value="${order.finalAmount}" type="number" groupingUsed="true" />đ</span></div>
        </div>
        <p style="margin-top:28px;color:#64748b">Nhân viên kiểm tra thông tin trước khi in hoặc lưu PDF giao cho khách.</p>
      </main>
    </div>
  </div>
</div>
</body>
</html>
