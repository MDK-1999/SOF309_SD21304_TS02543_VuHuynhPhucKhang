package com.pcshop.dao;
import com.pcshop.model.*; import com.pcshop.util.DBConnection; import com.pcshop.util.ModernCatalogSeeder;
import java.math.BigDecimal; import java.sql.*; import java.util.*;
public class OrderDAO {
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o=new Order(); o.setOrderId(rs.getInt("order_id"));
        o.setCustomerName(rs.getString("customer_name")); o.setCustomerPhone(rs.getString("customer_phone"));
        o.setCustomerEmail(rs.getString("customer_email")); o.setShippingAddr(rs.getString("shipping_addr"));
        o.setOrderType(rs.getString("order_type")); o.setStatus(rs.getString("status"));
        o.setPaymentMethod(rs.getString("payment_method")); o.setPaymentStatus(rs.getString("payment_status"));
        o.setNotes(rs.getString("notes")); o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDiscount(rs.getBigDecimal("discount")); o.setFinalAmount(rs.getBigDecimal("final_amount"));
        Timestamp t=rs.getTimestamp("created_at"); if(t!=null) o.setCreatedAt(t.toLocalDateTime());
        try{o.setStaffName(rs.getString("staff_name"));}catch(SQLException ignored){}
        try{o.setUserId(rs.getInt("user_id"));}catch(SQLException ignored){}
        return o;
    }
    public List<Order> getByCustomer(int userId) {
        ModernCatalogSeeder.ensureSeeded();
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name AS staff_name FROM Orders o LEFT JOIN Users u ON o.staff_id=u.user_id WHERE o.user_id=? ORDER BY o.created_at DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Order> getAll(int limit) {
        ModernCatalogSeeder.ensureSeeded();
        List<Order> list=new ArrayList<>();
        String sql="SELECT TOP(?) o.*, u.full_name AS staff_name FROM Orders o LEFT JOIN Users u ON o.staff_id=u.user_id ORDER BY o.created_at DESC";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,limit); ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public Order getById(int id) {
        ModernCatalogSeeder.ensureSeeded();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT o.*,u.full_name AS staff_name FROM Orders o LEFT JOIN Users u ON o.staff_id=u.user_id WHERE o.order_id=?")){
            ps.setInt(1,id); ResultSet rs=ps.executeQuery(); if(rs.next()) return mapRow(rs);
        } catch(SQLException e){e.printStackTrace();} return null;
    }
    public int create(Order o) {
        try (Connection c = DBConnection.getConnection()) {
            return insertOrder(c, o);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int createWithItems(Order order, List<OrderItem> items) {
        if (items == null || items.isEmpty()) return -1;
        Connection c = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement stock = c.prepareStatement("UPDATE Products SET stock_qty=stock_qty-? WHERE product_id=? AND is_active=1 AND stock_qty>=?")) {
                for (OrderItem item : items) {
                    stock.setInt(1, item.getQuantity());
                    stock.setInt(2, item.getProductId());
                    stock.setInt(3, item.getQuantity());
                    if (stock.executeUpdate() != 1) {
                        c.rollback();
                        return -2;
                    }
                }
            }
            int orderId = insertOrder(c, order);
            if (orderId <= 0) throw new SQLException("Order insert did not return an id");
            try (PreparedStatement detail = c.prepareStatement("INSERT INTO OrderItems(order_id,product_id,quantity,unit_price,subtotal) VALUES(?,?,?,?,?)")) {
                for (OrderItem item : items) {
                    item.setOrderId(orderId);
                    detail.setInt(1, orderId);
                    detail.setInt(2, item.getProductId());
                    detail.setInt(3, item.getQuantity());
                    detail.setBigDecimal(4, item.getUnitPrice());
                    detail.setBigDecimal(5, item.getSubtotal());
                    detail.addBatch();
                }
                int[] rows = detail.executeBatch();
                if (rows.length != items.size()) throw new SQLException("Order details were not inserted completely");
            }
            c.commit();
            return orderId;
        } catch (SQLException e) {
            rollbackQuietly(c);
            e.printStackTrace();
            return -1;
        } finally {
            closeQuietly(c);
        }
    }

    private int insertOrder(Connection c, Order o) throws SQLException {
        String sql="INSERT INTO Orders(user_id,staff_id,customer_name,customer_phone,customer_email,shipping_addr,order_type,total_amount,discount,final_amount,status,payment_method,notes) VALUES(?,?,?,?,?,?,?,?,?,?,'pending',?,?)";
        try (PreparedStatement ps=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if(o.getUserId()!=null) ps.setInt(1,o.getUserId()); else ps.setNull(1,Types.INTEGER);
            if(o.getStaffId()>0) ps.setInt(2,o.getStaffId()); else ps.setNull(2,Types.INTEGER);
            ps.setString(3,o.getCustomerName()); ps.setString(4,o.getCustomerPhone());
            ps.setString(5,o.getCustomerEmail()); ps.setString(6,o.getShippingAddr()); ps.setString(7,o.getOrderType());

            ps.setBigDecimal(8,o.getTotalAmount()); ps.setBigDecimal(9,o.getDiscount()); ps.setBigDecimal(10,o.getFinalAmount());
            ps.setString(11,o.getPaymentMethod()); ps.setString(12,o.getNotes());
            ps.executeUpdate();
            try (ResultSet rs=ps.getGeneratedKeys()) { if(rs.next()) return rs.getInt(1); }
        }
        return -1;
    }

    public boolean addItem(OrderItem item) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("INSERT INTO OrderItems(order_id,product_id,quantity,unit_price,subtotal) VALUES(?,?,?,?,?)")){
            ps.setInt(1,item.getOrderId()); ps.setInt(2,item.getProductId()); ps.setInt(3,item.getQuantity());
            ps.setBigDecimal(4,item.getUnitPrice()); ps.setBigDecimal(5,item.getSubtotal());
            return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public boolean updateAmounts(int orderId, BigDecimal total, BigDecimal discount, BigDecimal finalAmount) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Orders SET total_amount=?,discount=?,final_amount=?,updated_at=GETDATE() WHERE order_id=?")){
            ps.setBigDecimal(1,total); ps.setBigDecimal(2,discount); ps.setBigDecimal(3,finalAmount); ps.setInt(4,orderId);
            return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public List<OrderItem> getItems(int orderId) {
        ModernCatalogSeeder.ensureSeeded();
        List<OrderItem> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT oi.*,p.product_name FROM OrderItems oi JOIN Products p ON oi.product_id=p.product_id WHERE oi.order_id=?")){
            ps.setInt(1,orderId); ResultSet rs=ps.executeQuery();
            while(rs.next()){
                OrderItem i=new OrderItem(); i.setItemId(rs.getInt("item_id")); i.setOrderId(orderId);
                i.setProductId(rs.getInt("product_id")); i.setQuantity(rs.getInt("quantity"));
                i.setProductName(rs.getString("product_name")); i.setUnitPrice(rs.getBigDecimal("unit_price"));
                i.setSubtotal(rs.getBigDecimal("subtotal")); list.add(i);
            }
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public boolean updateStatus(int orderId, String status) {
        Connection c = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);
            String current;
            try (PreparedStatement read = c.prepareStatement("SELECT status FROM Orders WITH (UPDLOCK, ROWLOCK) WHERE order_id=?")) {
                read.setInt(1, orderId);
                try (ResultSet rs = read.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return false; }
                    current = rs.getString(1);
                }
            }
            if (Objects.equals(current, status)) { c.commit(); return true; }
            if (!"cancelled".equals(current) && "cancelled".equals(status)) {
                adjustOrderStock(c, orderId, true);
            } else if ("cancelled".equals(current) && !"cancelled".equals(status)) {
                if (!adjustOrderStock(c, orderId, false)) { c.rollback(); return false; }
            }
            try (PreparedStatement update = c.prepareStatement("UPDATE Orders SET status=?,updated_at=GETDATE() WHERE order_id=?")) {
                update.setString(1, status);
                update.setInt(2, orderId);
                if (update.executeUpdate() != 1) throw new SQLException("Order status was not updated");
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            rollbackQuietly(c);
            e.printStackTrace();
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    private boolean adjustOrderStock(Connection c, int orderId, boolean restore) throws SQLException {
        String sql = restore
                ? "UPDATE p SET p.stock_qty=p.stock_qty+oi.quantity FROM Products p JOIN OrderItems oi ON p.product_id=oi.product_id WHERE oi.order_id=?"
                : "UPDATE p SET p.stock_qty=p.stock_qty-oi.quantity FROM Products p JOIN OrderItems oi ON p.product_id=oi.product_id WHERE oi.order_id=? AND p.stock_qty>=oi.quantity";
        int expected;
        try (PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM OrderItems WHERE order_id=?")) {
            count.setInt(1, orderId);
            try (ResultSet rs = count.executeQuery()) { expected = rs.next() ? rs.getInt(1) : 0; }
        }
        try (PreparedStatement update = c.prepareStatement(sql)) {
            update.setInt(1, orderId);
            return expected > 0 && update.executeUpdate() == expected;
        }
    }

    public boolean updatePayment(int orderId, String payStatus) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Orders SET payment_status=?,updated_at=GETDATE() WHERE order_id=?")){
            ps.setString(1,payStatus); ps.setInt(2,orderId); return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public int countByStatus(String status) {
        ModernCatalogSeeder.ensureSeeded();
        String sql=status==null?"SELECT COUNT(*) FROM Orders":"SELECT COUNT(*) FROM Orders WHERE status=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            if(status!=null) ps.setString(1,status); ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch(SQLException e){e.printStackTrace();} return 0;
    }
    public BigDecimal getTodayRevenue() {
        ModernCatalogSeeder.ensureSeeded();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT ISNULL(SUM(final_amount),0) FROM Orders WHERE CAST(created_at AS DATE)=CAST(GETDATE() AS DATE) AND status NOT IN ('cancelled')")){
            ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getBigDecimal(1);
        } catch(SQLException e){e.printStackTrace();} return BigDecimal.ZERO;
    }
    public BigDecimal getMonthRevenue() {
        ModernCatalogSeeder.ensureSeeded();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT ISNULL(SUM(final_amount),0) FROM Orders WHERE MONTH(created_at)=MONTH(GETDATE()) AND YEAR(created_at)=YEAR(GETDATE()) AND status NOT IN ('cancelled')")){
            ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getBigDecimal(1);
        } catch(SQLException e){e.printStackTrace();} return BigDecimal.ZERO;
    }
    public List<Object[]> getWeeklyRevenue() {
        ModernCatalogSeeder.ensureSeeded();
        List<Object[]> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT CAST(created_at AS DATE) AS revenue_day, COUNT(*) AS cnt, ISNULL(SUM(final_amount),0) AS rev FROM Orders WHERE created_at>=DATEADD(DAY,-6,CAST(GETDATE() AS DATE)) AND status<>'cancelled' GROUP BY CAST(created_at AS DATE) ORDER BY revenue_day")){
            ResultSet rs=ps.executeQuery();
            while(rs.next()) list.add(new Object[]{rs.getString("revenue_day"),rs.getInt("cnt"),rs.getBigDecimal("rev")});
        } catch(SQLException e){e.printStackTrace();} return list;
    }

    private void rollbackQuietly(Connection c) {
        if (c == null) return;
        try { c.rollback(); } catch (SQLException ignored) { }
    }

    private void closeQuietly(Connection c) {
        if (c == null) return;
        try { c.close(); } catch (SQLException ignored) { }
    }}
