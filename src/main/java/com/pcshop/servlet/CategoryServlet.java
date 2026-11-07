package com.pcshop.servlet;

import com.pcshop.dao.CategoryDAO;
import com.pcshop.model.Category;
import com.pcshop.util.InputValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/categories")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CategoryDAO dao = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("categories", dao.getAll());
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = InputValidator.text(req.getParameter("action"), 16);
        Category category = new Category();
        category.setCatName(InputValidator.text(req.getParameter("catName"), 100));
        category.setDescription(InputValidator.multiline(req.getParameter("description"), 500));
        category.setIcon(InputValidator.text(req.getParameter("icon"), 50));
        category.setActive("add".equals(action) || "true".equals(req.getParameter("isActive")));
        boolean valid = category.getCatName() != null && category.getCatName().length() >= 2;
        boolean success = false;
        if (valid && "add".equals(action)) success = dao.add(category);
        else if (valid && "update".equals(action)) {
            category.setCatId(InputValidator.positiveInt(req.getParameter("catId"), -1, Integer.MAX_VALUE));
            success = category.getCatId() > 0 && dao.update(category);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories?" + (success ? "success=1" : "error=invalid"));
    }
}
