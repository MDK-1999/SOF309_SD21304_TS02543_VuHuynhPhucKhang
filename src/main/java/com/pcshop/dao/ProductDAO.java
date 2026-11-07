package com.pcshop.dao;
import com.pcshop.model.Product; import com.pcshop.util.DBConnection; import com.pcshop.util.ModernCatalogSeeder; import com.pcshop.util.ProductPresentation;
import java.math.BigDecimal; import java.sql.*; import java.util.*;
public class ProductDAO {
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p=new Product(); p.setProductId(rs.getInt("product_id")); p.setCatId(rs.getInt("cat_id"));
        p.setProductName(rs.getString("product_name")); p.setBrand(rs.getString("brand"));
        p.setModel(rs.getString("model")); p.setDescription(rs.getString("description"));
        p.setSpecs(rs.getString("specs")); p.setPrice(rs.getBigDecimal("price"));
        try{p.setImageUrl(rs.getString("image_url"));}catch(SQLException ignored){}
        p.setStockQty(rs.getInt("stock_qty")); p.setActive(rs.getBoolean("is_active"));
        try{p.setCatName(rs.getString("cat_name"));}catch(SQLException ignored){}
        try{Timestamp t=rs.getTimestamp("created_at"); if(t!=null) p.setCreatedAt(t.toLocalDateTime());}catch(SQLException ignored){}
        return ProductPresentation.enrich(p);
    }
    public List<Product> getAll() {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id ORDER BY p.product_name")){
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<Product> getActive() {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.is_active=1 ORDER BY p.product_name")){
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public Product getById(int id) {
        ModernCatalogSeeder.ensureSeeded();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.product_id=?")){
            ps.setInt(1,id); ResultSet rs=ps.executeQuery(); if(rs.next()) return mapRow(rs);
        } catch(SQLException e){e.printStackTrace();} return null;
    }
    public List<Product> search(String q) {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.product_name LIKE ? OR p.brand LIKE ?")){
            String like="%"+q+"%"; ps.setString(1,like); ps.setString(2,like);
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<Product> searchActive(String q, Integer catId, String brand, BigDecimal minPrice, BigDecimal maxPrice) {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        StringBuilder sql=new StringBuilder("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.is_active=1");
        List<Object> params=new ArrayList<>();
        if(q!=null && !q.isBlank()){ sql.append(" AND (p.product_name LIKE ? OR p.brand LIKE ? OR p.model LIKE ? OR p.specs LIKE ?)"); String like="%"+q.trim()+"%"; params.add(like); params.add(like); params.add(like); params.add(like); }
        if(catId!=null){ sql.append(" AND p.cat_id=?"); params.add(catId); }
        if(brand!=null && !brand.isBlank()){ sql.append(" AND p.brand=?"); params.add(brand); }
        if(minPrice!=null){ sql.append(" AND p.price>=?"); params.add(minPrice); }
        if(maxPrice!=null){ sql.append(" AND p.price<=?"); params.add(maxPrice); }
        sql.append(" ORDER BY p.created_at DESC, p.product_name");
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++) ps.setObject(i+1,params.get(i));
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<Product> searchActivePage(String q, Integer catId, String brand, BigDecimal minPrice, BigDecimal maxPrice, String sort, int offset, int limit) {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = activeFilter(q, catId, brand, minPrice, maxPrice, params);
        String orderBy = switch (sort == null ? "" : sort) {
            case "price-asc" -> "p.price ASC, p.product_id DESC";
            case "price-desc" -> "p.price DESC, p.product_id DESC";
            case "stock" -> "p.stock_qty DESC, p.product_id DESC";
            case "name" -> "p.product_name ASC, p.product_id DESC";
            default -> "p.created_at DESC, p.product_id DESC";
        };
        String sql = "SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id " + where
                + " ORDER BY " + orderBy + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int index = bind(ps, params);
            ps.setInt(index++, Math.max(0, offset));
            ps.setInt(index, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countActive(String q, Integer catId, String brand, BigDecimal minPrice, BigDecimal maxPrice) {
        ModernCatalogSeeder.ensureSeeded();
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM Products p " + activeFilter(q, catId, brand, minPrice, maxPrice, params);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String activeFilter(String q, Integer catId, String brand, BigDecimal minPrice, BigDecimal maxPrice, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE p.is_active=1");
        if (q != null && !q.isBlank()) {
            where.append(" AND (p.product_name LIKE ? OR p.brand LIKE ? OR p.model LIKE ? OR p.specs LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (catId != null) { where.append(" AND p.cat_id=?"); params.add(catId); }
        if (brand != null && !brand.isBlank()) { where.append(" AND p.brand=?"); params.add(brand); }
        if (minPrice != null) { where.append(" AND p.price>=?"); params.add(minPrice); }
        if (maxPrice != null) { where.append(" AND p.price<=?"); params.add(maxPrice); }
        return where.toString();
    }

    private int bind(PreparedStatement statement, List<Object> params) throws SQLException {
        int index = 1;
        for (Object value : params) statement.setObject(index++, value);
        return index;
    }
    public List<Product> getByCategory(int catId) {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.is_active=1 AND p.cat_id=? ORDER BY p.price")){
            ps.setInt(1,catId); ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<Product> getLatest(int limit) {
        ModernCatalogSeeder.ensureSeeded();
        List<Product> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT TOP(?) p.*,c.cat_name FROM Products p JOIN Categories c ON p.cat_id=c.cat_id WHERE p.is_active=1 ORDER BY p.created_at DESC, p.product_id DESC")){
            ps.setInt(1,limit); ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<String> getActiveBrands() {
        ModernCatalogSeeder.ensureSeeded();
        List<String> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT DISTINCT brand FROM Products WHERE is_active=1 AND brand IS NOT NULL AND brand<>'' ORDER BY brand")){
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(rs.getString(1));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public boolean add(Product p) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("INSERT INTO Products(cat_id,product_name,brand,model,description,price,stock_qty,specs) VALUES(?,?,?,?,?,?,?,?)")){
            ps.setInt(1,p.getCatId()); ps.setString(2,p.getProductName()); ps.setString(3,p.getBrand());
            ps.setString(4,p.getModel()); ps.setString(5,p.getDescription()); ps.setBigDecimal(6,p.getPrice());
            ps.setInt(7,p.getStockQty()); ps.setString(8,p.getSpecs()); return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public boolean update(Product p) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Products SET cat_id=?,product_name=?,brand=?,model=?,description=?,price=?,specs=?,is_active=? WHERE product_id=?")){
            ps.setInt(1,p.getCatId()); ps.setString(2,p.getProductName()); ps.setString(3,p.getBrand());
            ps.setString(4,p.getModel()); ps.setString(5,p.getDescription()); ps.setBigDecimal(6,p.getPrice());
            ps.setString(7,p.getSpecs()); ps.setBoolean(8,p.isActive()); ps.setInt(9,p.getProductId());
            return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public boolean updateStock(int productId, int delta) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Products SET stock_qty=stock_qty+? WHERE product_id=?")){
            ps.setInt(1,delta); ps.setInt(2,productId); return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public int countAll() {
        ModernCatalogSeeder.ensureSeeded();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM Products WHERE is_active=1")){
            ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch(SQLException e){e.printStackTrace();} return 0;
    }
    public int countLowStock(int threshold) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM Products WHERE stock_qty<=? AND is_active=1")){
            ps.setInt(1,threshold); ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch(SQLException e){e.printStackTrace();} return 0;
    }
}
