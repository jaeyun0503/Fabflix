import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * servlet filter implementation class EmployeeLoginFilter
 */
@WebFilter(filterName = "EmployeeLoginFilter", urlPatterns = "/_dashboard/*")
public class EmployeeLoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        Object userObj = httpRequest.getSession().getAttribute("user");
        if (!(userObj instanceof Employee)) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/employee-login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(allowedURI -> requestURI.endsWith(allowedURI)) ||
                allowedURIs.stream().anyMatch(requestURI::equals);
    }

    public void init(FilterConfig fConfig) {
        String contextPath = fConfig.getServletContext().getContextPath();
        allowedURIs.add(contextPath + "/style.css");
        allowedURIs.add(contextPath + "/employee-login.html");
        allowedURIs.add(contextPath + "/employee-login.js");
        allowedURIs.add(contextPath + "/api/dashboard_login");
    }

    public void destroy() {
        // ignored
    }
}