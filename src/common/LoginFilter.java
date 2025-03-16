package common;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class common.LoginFilter
 */
@WebFilter(filterName = "common.LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("common.LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        String token = JwtUtil.getCookieValue(httpRequest, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (claims != null) {
            // Store claims in request attributes
            // Downstream servlets can use claims as the session storage
            httpRequest.setAttribute("claims", claims);

            // Proceed with the request
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect("login.html");
        }
    }


    private boolean isUrlAllowedWithoutLogin(String requestURI) {

        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }


    public void init(FilterConfig fConfig) {

        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("style.css");
        allowedURIs.add("api/dashboard_login");
        allowedURIs.add("_dashboard");
    }

    public void destroy() {
        // ignored.
    }
}
