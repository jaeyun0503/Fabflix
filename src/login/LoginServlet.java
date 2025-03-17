package login;

import com.google.gson.JsonObject;
import common.JwtUtil;
import common.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/ReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (VerifyPassword.validPassword(username, password, "customer")) {
//                    User user = new User(username);
//                    String id = rs.getString("id");
//                    user.setId(Integer.parseInt(id));
//                    request.getSession().setAttribute("user", user);
                    String subject = username;

                    // store user login time in JWT
                    Map<String, Object> claims = new HashMap<>();

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    claims.put("loginTime", dateFormat.format(new Date()));

                    // Generate new JWT and add it to Header
                    String token = JwtUtil.generateToken(subject, claims);
                    JwtUtil.updateJwtCookie(request, response, token);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "Incorrect Password. Please try again.");

                }
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "The username does not exist. Please try again");
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Internal Server Error");
            e.printStackTrace();
            response.setStatus(500);
        }

        String jsonResponse = responseJsonObject.toString();
        System.out.println("Response JSON: " + jsonResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
