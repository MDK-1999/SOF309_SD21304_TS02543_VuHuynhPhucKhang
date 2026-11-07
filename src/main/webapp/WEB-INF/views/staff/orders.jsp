<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="GearAstral Manager - Đơn hàng" />
<!DOCTYPE html><html lang="vi"><head>
<%@ include file="/WEB-INF/views/common/_manager-head.jspf" %>
</head><body>
<div class="app-wrap">
  <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
  <div class="main">
    <div class="topbar">
      <div class="topbar-title">📦 Quản lý Đơn hàng</div>
      <div class="topbar-right">
        <button class="btn btn-primary btn-sm" onclick="showModal('createModal')">+ Tạo đơn hàng</button>
      </div>
    </div>
    <div class="content fade-in">
      <c:if test="${not empty sessionScope.flashType}"><div id="toast" class="toast ${fn:escapeXml(sessionScope.flashType)}"><div class="toast-icon">${sessionScope.flashType eq 'error' ? '❌' : '✅'}</div><div class="toast-body"><div class="toast-title"><c:out value="${sessionScope.flashTitle}"/></div><div class="toast-msg"><c:out value="${sessionScope.flashMsg}"/></div></div><button class="toast-close" onclick="closeToast()">✕</button><div class="toast-bar"></div></div></c:if>
      <c:remove var="flashType" scope="session"/><c:remove var="flashTitle" scope="session"/><c:remove var="flashMsg" scope="session"/>
      <c:if test="${param.success != null}"><div class="alert alert-success">✅ Thao tác thành công!</div></c:if>
      <c:if test="${param.error != null}"><div class="alert alert-danger">Không lưu được trạng thái. Vui lòng thử lại.</div></c:if>

      <div class="card">
        <div class="card-header">
          <div class="card-title">📦 Danh sách đơn hàng</div>
          <input type="text" class="form-control" style="width:240px" placeholder="Tìm đơn hàng..." oninput="filterTable(this.value,'otbl')"/>
        </div>
        <div class="table-wrap">
          <table id="otbl">
            <thead><tr><th>#</th><th>Khách hàng</th><th>SĐT</th><th>Tổng tiền</th><th>TT đơn</th><th>TT thanh toán</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
            <tbody>
              <c:forEach var="o" items="${orders}" varStatus="st">
              <tr>
                <td class="text-sub fw-700">#${o.orderId}</td>
                <td><strong><c:out value="${o.customerName}"/></strong><br/><small class="text-sub"><c:out value="${o.customerEmail}"/></small></td>
                <td><c:out value="${o.customerPhone}"/></td>
                <td class="fw-700 text-primary"><fmt:formatNumber value="${o.finalAmount}" pattern="#,###"/>đ</td>
                <td><span class="badge badge-${fn:escapeXml(o.status)}">
                  <c:choose>
                    <c:when test="${o.status == 'pending'}">⏳ Chờ xử lý</c:when>
                    <c:when test="${o.status == 'confirmed'}">✅ Đã xác nhận</c:when>
                    <c:when test="${o.status == 'processing'}">🔧 Đang xử lý</c:when>
                    <c:when test="${o.status == 'shipping'}">🚚 Đang giao hàng</c:when>
                    <c:when test="${o.status == 'done'}">✔ Hoàn thành</c:when>
                    <c:when test="${o.status == 'cancelled'}">✖ Đã hủy</c:when>
                    <c:otherwise><c:out value="${o.status}"/></c:otherwise>
                  </c:choose>
                </span></td>
                <td><span class="badge badge-${fn:escapeXml(o.paymentStatus)}">
                  <c:choose>
                    <c:when test="${o.paymentStatus == 'unpaid'}">Chưa thanh toán</c:when>
                    <c:when test="${o.paymentStatus == 'paid'}">Đã thanh toán</c:when>
                    <c:when test="${o.paymentStatus == 'refunded'}">Đã hoàn tiền</c:when>
                    <c:otherwise><c:out value="${o.paymentStatus}"/></c:otherwise>
                  </c:choose>
                </span></td>
                <td style="font-size:0.8rem;color:var(--text-sub)"><c:out value="${o.createdAt}"/></td>
                <td>
                  <div style="display:flex;gap:4px;flex-wrap:wrap">
                    <button class="btn btn-sm btn-outline btn-icon order-status-btn" type="button"
                      data-order-id="${o.orderId}" data-status="${fn:escapeXml(o.status)}" data-payment-status="${fn:escapeXml(o.paymentStatus)}">⚙️</button>
                    <a class="btn btn-sm btn-success" href="${pageContext.request.contextPath}/staff/invoice?id=${o.orderId}">Bill</a>
                  </div>
                </td>
              </tr>
              </c:forEach>
              <c:if test="${empty orders}"><tr><td colspan="8"><div class="empty-state"><div class="empty-icon">📦</div><p>Chưa có đơn hàng nào</p></div></td></tr></c:if>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- TẠO ĐƠN HÀNG MODAL -->
<div id="createModal" class="modal hidden">
  <div class="modal-box" style="max-width:680px">
    <div class="modal-header">
      <span class="modal-title">🛒 Tạo đơn hàng mới</span>
      <button class="modal-close" onclick="hideModal('createModal')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/staff/orders">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
      <input type="hidden" name="action" value="create"/>
      <div class="modal-body">
        <div class="form-row">
          <div class="form-group"><label>Tên khách hàng *</label><input type="text" name="customerName" required/></div>
          <div class="form-group"><label>Số điện thoại</label><input type="text" name="customerPhone"/></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Email</label><input type="email" name="customerEmail"/></div>
          <div class="form-group"><label>Hình thức thanh toán</label>
            <select name="paymentMethod">
              <option value="cod">COD - thanh toán khi nhận hàng</option>
              <option value="qr">Thanh toán bằng mã QR</option>
            </select>
          </div>
        </div>
        <div class="form-group"><label>Địa chỉ giao hàng</label><input type="text" name="shippingAddr"/></div>

        <!-- Chọn sản phẩm -->
        <div style="margin:16px 0 8px;font-weight:700;font-size:0.88rem;color:var(--text-main)">🖥️ Thêm sản phẩm</div>
        <div id="orderItems">
          <div class="order-item-row" style="display:flex;gap:8px;margin-bottom:8px;align-items:flex-end">
            <div class="form-group" style="flex:3;margin:0">
              <label>Sản phẩm</label>
              <select name="productId[]" class="product-select" onchange="updatePrice(this)">
                <option value="">-- Chọn sản phẩm --</option>
                <c:forEach var="p" items="${products}">
                <option value="${p.productId}" data-price="${p.price}"><c:out value="${p.productName}"/> — <fmt:formatNumber value="${p.price}" pattern="#,###"/>đ (${p.stockQty})</option>
                </c:forEach>
              </select>
            </div>
            <div class="form-group" style="flex:1;margin:0">
              <label>SL</label><input type="number" name="quantity[]" value="1" min="1" style="width:100%"/>
            </div>
            <div class="form-group" style="flex:1.5;margin:0">
              <label>Thành tiền</label><input type="text" class="subtotal" readonly style="background:#f8fafc"/>
            </div>
            <button type="button" onclick="removeItem(this)" class="btn btn-danger btn-icon" style="margin-bottom:1px">🗑️</button>
          </div>
        </div>
        <button type="button" class="btn btn-ghost btn-sm" onclick="addItem()">+ Thêm sản phẩm</button>

        <div style="margin:16px 0 8px;padding-top:16px;border-top:1px solid var(--border);display:flex;justify-content:flex-end;align-items:center;gap:12px">
          <span style="font-weight:700">Tổng cộng:</span>
          <span id="grandTotal" style="font-family:var(--font-head);font-size:1.3rem;font-weight:700;color:var(--primary)">0đ</span>
        </div>
        <div class="form-group"><label>Ghi chú</label><textarea name="notes" rows="2"></textarea></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-ghost" onclick="hideModal('createModal')">Hủy</button>
        <button type="submit" class="btn btn-primary">💾 Tạo đơn hàng</button>
      </div>
    </form>
  </div>
</div>

<!-- CẬP NHẬT TRẠNG THÁI MODAL -->
<div id="statusModal" class="modal hidden">
  <div class="modal-box" style="max-width:380px">
    <div class="modal-header">
      <span class="modal-title">⚙️ Cập nhật đơn hàng #<span id="sOrderId"></span></span>
      <button class="modal-close" onclick="hideModal('statusModal')">✕</button>
    </div>
    <div class="modal-body">
      <form method="post" action="${pageContext.request.contextPath}/staff/orders" id="statusForm">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
        <input type="hidden" name="action" value="status"/>
        <input type="hidden" name="orderId" id="sOrderIdInput"/>
        <div class="form-group">
          <label>Trạng thái đơn hàng</label>
          <select name="status" id="sStatus">
            <option value="pending">⏳ Chờ xử lý</option>
            <option value="confirmed">✅ Đã xác nhận</option>
            <option value="processing">🔧 Đang xử lý</option>
            <option value="shipping">🚚 Đang giao hàng</option>
            <option value="done">✔️ Hoàn thành</option>
            <option value="cancelled">❌ Đã hủy</option>
          </select>
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%">Cập nhật trạng thái</button>
      </form>
      <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="margin-top:12px">
      <input type="hidden" name="_csrf" value="${csrfToken}" />
        <input type="hidden" name="action" value="payment"/>
        <input type="hidden" name="orderId" id="sOrderIdPay"/>
        <div class="form-group">
          <label>Trạng thái thanh toán</label>
          <select name="paymentStatus" id="sPayStatus">
            <option value="unpaid">💳 Chưa thanh toán</option>
            <option value="paid">✅ Đã thanh toán</option>
            <option value="refunded">↩️ Đã hoàn tiền</option>
          </select>
        </div>
        <button type="submit" class="btn btn-success" style="width:100%">Cập nhật thanh toán</button>
      </form>
    </div>
  </div>
</div>

<script>

function openStatusModal(id,status,payStatus){
  document.getElementById('sOrderId').textContent=id;
  document.getElementById('sOrderIdInput').value=id;
  document.getElementById('sOrderIdPay').value=id;
  document.getElementById('sStatus').value=status;
  document.getElementById('sPayStatus').value=payStatus;
  showModal('statusModal');
}

function updatePrice(sel){
  const opt=sel.options[sel.selectedIndex];
  const price=parseFloat(opt.dataset.price||0);
  const row=sel.closest('.order-item-row');
  const qty=parseInt(row.querySelector('input[name="quantity[]"]').value)||1;
  const sub=price*qty;
  row.querySelector('.subtotal').value=sub.toLocaleString('vi')+'đ';
  calcTotal();
}
function calcTotal(){
  let total=0;
  document.querySelectorAll('.order-item-row').forEach(row=>{
    const sel=row.querySelector('.product-select');
    const opt=sel.options[sel.selectedIndex];
    const price=parseFloat(opt?.dataset?.price||0);
    const qty=parseInt(row.querySelector('input[name="quantity[]"]').value)||1;
    total+=price*qty;
  });
  document.getElementById('grandTotal').textContent=total.toLocaleString('vi')+'đ';
}
function addItem(){
  const tpl=document.querySelector('.order-item-row').cloneNode(true);
  tpl.querySelector('.product-select').value='';
  tpl.querySelector('input[name="quantity[]"]').value='1';
  tpl.querySelector('.subtotal').value='';
  tpl.querySelector('.product-select').addEventListener('change',function(){updatePrice(this);});
  tpl.querySelector('input[name="quantity[]"]').addEventListener('input',function(){updatePrice(tpl.querySelector('.product-select'));});
  document.getElementById('orderItems').appendChild(tpl);
}
function removeItem(btn){
  const rows=document.querySelectorAll('.order-item-row');
  if(rows.length>1){btn.closest('.order-item-row').remove();calcTotal();}
}
document.querySelectorAll('.product-select').forEach(s=>s.addEventListener('change',function(){updatePrice(this);}));
document.querySelectorAll('input[name="quantity[]"]').forEach(i=>i.addEventListener('input',function(){updatePrice(this.closest('.order-item-row').querySelector('.product-select'));}));
document.querySelectorAll('.order-status-btn').forEach((button) => {
  button.addEventListener('click', () => openStatusModal(
    button.dataset.orderId,
    button.dataset.status,
    button.dataset.paymentStatus
  ));
});
</script>
</body></html>
