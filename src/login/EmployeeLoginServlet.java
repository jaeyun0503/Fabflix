package login;

import com.google.gson.JsonObject;
import common.Employee;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "login.EmployeeLoginServlet", urlPatterns = {"/api/dashboard_login", "/_dashboard"})
public class EmployeeLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/ReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            // User is logged in, redirect to the dashboard page
            Object userObject = session.getAttribute("user");
            if (userObject instanceof Employee) {
                response.sendRedirect("metadata.html");
                return;
            }
            else {
                // No user is logged in, redirect to the login page
                response.sendRedirect("employee-login.html");
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = String.format("SELECT * FROM employees WHERE email='%s'", email);

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if (rs.next()) {
                if (VerifyPassword.validPassword(email, password, "employee")) {
                    String e_email = rs.getString("email");
                    String e_password = rs.getString("password");
                    request.getSession().setAttribute("user", new Employee(e_email, e_password));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
                else {
                    // invalid email
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "Incorrect Password. Please try again.");

                }
            } else {
                // Invalid email
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "The account does not exist. Please try again.");
            }

            rs.close();
            statement.close();
            out.write(responseJsonObject.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);

        } finally {
            out.close();
        }
    }

}