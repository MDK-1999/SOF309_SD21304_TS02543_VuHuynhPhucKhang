package com.pcshop.filter;

import com.pcshop.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

public class AuthFilter implements Filter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/", "/index.jsp", "/login", "/register", "/shop", "/product",
            "/product-image", "/cart", "/checkout", "/pc-builder", "/ai-chat",
            "/robots.txt", "/sitemap.xml");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = requestPath(req);

        if (PUBLIC_PATHS.contains(path) || isPublicAsset(path) || !isProtectedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loginUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=session");
            return;
        }

        if (path.startsWith("/admin/") && !user.isAdmin()) {
            deny(req, resp);
            return;
        }
        if (path.startsWith("/staff/") && !user.isStaff()) {
            deny(req, resp);
            return;
        }
        if (path.startsWith("/customer/") && !user.isCustomer()) {
            deny(req, resp);
            return;
        }
        chain.doFilter(request, response);
    }

    private void deny(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/dashboard?error=forbidden");
    }

    private boolean isProtectedPath(String path) {
        return path.equals("/dashboard") || path.equals("/logout")
                || path.startsWith("/admin/") || path.startsWith("/staff/")
                || path.startsWith("/customer/");
    }

    private boolean isPublicAsset(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }

    private String requestPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        return uri.substring(Math.min(context.length(), uri.length()));
    }
}
