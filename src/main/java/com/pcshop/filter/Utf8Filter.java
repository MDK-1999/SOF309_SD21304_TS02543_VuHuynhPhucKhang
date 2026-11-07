package com.pcshop.filter;
import jakarta.servlet.*; import java.io.IOException;
public class Utf8Filter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8"); resp.setCharacterEncoding("UTF-8"); chain.doFilter(req, resp);
    }
}
