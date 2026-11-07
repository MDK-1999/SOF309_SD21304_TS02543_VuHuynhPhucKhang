package com.pcshop.dao;
import com.pcshop.model.Category; import com.pcshop.util.DBConnection; import com.pcshop.util.ModernCatalogSeeder;
import java.sql.*; import java.util.*;
public class CategoryDAO {
    private Category mapRow(ResultSet rs) throws SQLException {
        Category c=new Category(); c.setCatId(rs.getInt("cat_id")); c.setCatName(rs.getString("cat_name"));
        c.setDescription(rs.getString("description")); c.setIcon(rs.getString("icon")); c.setActive(rs.getBoolean("is_active")); return c;
    }
    public List<Category> getAll() {
        ModernCatalogSeeder.ensureSeeded();
        List<Category> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT * FROM Categories ORDER BY cat_name")){
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public List<Category> getActive() {
        ModernCatalogSeeder.ensureSeeded();
        List<Category> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT * FROM Categories WHERE is_active=1 ORDER BY cat_name")){
            ResultSet rs=ps.executeQuery(); while(rs.next()) list.add(mapRow(rs));
        } catch(SQLException e){e.printStackTrace();} return list;
    }
    public boolean add(Category cat) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("INSERT INTO Categories(cat_name,description,icon) VALUES(?,?,?)")){
            ps.setString(1,cat.getCatName()); ps.setString(2,cat.getDescription()); ps.setString(3,cat.getIcon());
            return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
    public boolean update(Category cat) {
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("UPDATE Categories SET cat_name=?,description=?,icon=?,is_active=? WHERE cat_id=?")){
            ps.setString(1,cat.getCatName()); ps.setString(2,cat.getDescription()); ps.setString(3,cat.getIcon());
            ps.setBoolean(4,cat.isActive()); ps.setInt(5,cat.getCatId()); return ps.executeUpdate()>0;
        } catch(SQLException e){e.printStackTrace(); return false;}
    }
}
