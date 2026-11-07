package com.pcshop.servlet;

import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/robots.txt", "/sitemap.xml"})
public class SeoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/robots.txt".equals(req.getServletPath())) writeRobots(req, resp);
        else writeSitemap(req, resp);
    }

    private void writeRobots(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().printf("User-agent: *%nAllow: /%nDisallow: %s/admin/%nDisallow: %s/staff/%nDisallow: %s/customer/%nDisallow: %s/cart%nDisallow: %s/checkout%nSitemap: %s%s/sitemap.xml%n",
                req.getContextPath(), req.getContextPath(), req.getContextPath(), req.getContextPath(), req.getContextPath(), baseUrl(req), req.getContextPath());
    }

    private void writeSitemap(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/xml;charset=UTF-8");
        String base = baseUrl(req) + req.getContextPath();
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        appendUrl(xml, base + "/shop", "daily", "1.0");
        appendUrl(xml, base + "/pc-builder", "weekly", "0.9");
        for (Product product : productDAO.getActive()) {
            appendUrl(xml, base + "/product?id=" + product.getProductId(), "weekly", "0.8");
        }
        xml.append("</urlset>");
        resp.getWriter().write(xml.toString());
    }

    private void appendUrl(StringBuilder xml, String url, String frequency, String priority) {
        xml.append("  <url><loc>").append(escapeXml(url)).append("</loc><changefreq>")
                .append(frequency).append("</changefreq><priority>").append(priority).append("</priority></url>\n");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private String baseUrl(HttpServletRequest req) {
        int port = req.getServerPort();
        boolean defaultPort = (req.isSecure() && port == 443) || (!req.isSecure() && port == 80);
        return req.getScheme() + "://" + req.getServerName() + (defaultPort ? "" : ":" + port);
    }
}
