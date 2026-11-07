package com.pcshop.servlet;

import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import com.pcshop.model.User;
import com.pcshop.util.DBConnection;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/staff/stock")
public class StockServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAX_COST = new BigDecimal("1000000000");
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("products", productDAO.getAll());
        req.setAttribute("imports", getRecentImports());
        req.getRequestDispatcher("/WEB-INF/views/staff/stock.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User staff = (User) req.getSession().getAttribute("loginUser");
        int productId = InputValidator.positiveInt(req.getParameter("productId"), -1, Integer.MAX_VALUE);
        int quantity = InputValidator.positiveInt(req.getParameter("quantity"), -1, 100_000);
        BigDecimal unitCost = InputValidator.money(req.getParameter("unitCost"), BigDecimal.ZERO, MAX_COST);
        Product product = productId > 0 ? productDAO.getById(productId) : null;
        if (staff == null || product == null || quantity < 1) {
            resp.sendRedirect(req.getContextPath() + "/staff/stock?error=invalid");
            return;
        }
        String supplier = InputValidator.text(req.getParameter("supplier"), 150);
        String note = InputValidator.multiline(req.getParameter("note"), 500);
        boolean success = importStock(productId, staff.getUserId(), quantity, unitCost, supplier, note);
        resp.sendRedirect(req.getContextPath() + "/staff/stock?" + (success ? "success=imported" : "error=database"));
    }

    private boolean importStock(int productId, int staffId, int quantity, BigDecimal unitCost, String supplier, String note) {
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO StockImports(product_id,staff_id,quantity,unit_cost,supplier,note) VALUES(?,?,?,?,?,?)");
                 PreparedStatement update = connection.prepareStatement("UPDATE Products SET stock_qty=stock_qty+? WHERE product_id=?")) {
                insert.setInt(1, productId); insert.setInt(2, staffId); insert.setInt(3, quantity);
                insert.setBigDecimal(4, unitCost); insert.setString(5, supplier); insert.setString(6, note);
                if (insert.executeUpdate() != 1) throw new SQLException("Stock import was not inserted");
                update.setInt(1, quantity); update.setInt(2, productId);
                if (update.executeUpdate() != 1) throw new SQLException("Product stock was not updated");
            }
            connection.commit();
            return true;
        } catch (SQLException error) {
            if (connection != null) try { connection.rollback(); } catch (SQLException ignored) { }
            error.printStackTrace();
            return false;
        } finally {
            if (connection != null) try { connection.close(); } catch (SQLException ignored) { }
        }
    }

    private List<Object[]> getRecentImports() {
        List<Object[]> imports = new ArrayList<>();
        String sql = "SELECT TOP 30 si.*,p.product_name,u.full_name AS staff_name FROM StockImports si JOIN Products p ON si.product_id=p.product_id LEFT JOIN Users u ON si.staff_id=u.user_id ORDER BY si.import_date DESC";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) imports.add(new Object[]{result.getString("product_name"), result.getInt("quantity"), result.getBigDecimal("unit_cost"), result.getString("supplier"), result.getString("staff_name"), result.getTimestamp("import_date")});
        } catch (SQLException error) {
            error.printStackTrace();
        }
        return imports;
    }
}
