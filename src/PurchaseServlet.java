import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.protobuf.TextFormat;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

//"/api/purhcase"
@WebServlet(name = "PurchaseServlet", urlPatterns = "/api/purchase")
public class PurchaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expirationDate = request.getParameter("expirationDate");
        Connection conn = null;
        boolean valid = false;
        try {
            conn = dataSource.getConnection();
            String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, cardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, expirationDate);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                valid = true;
            }
            if (!valid) { // Incorrect
                request.setAttribute("errorMessage", "Invalid credit card information. Please try again.");
                request.getRequestDispatcher("payment.html").forward(request, response);
                return;
            }
            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("shoppingCart");
            User user = (User) session.getAttribute("user");
            System.out.println(user.getId());
            if (cart == null || cart.getItems().isEmpty()) {
                request.setAttribute("errorMessage", "Your shopping cart is empty.");
                request.getRequestDispatcher("payment.html").forward(request, response);
                return;
            }
            conn.setAutoCommit(false);
            String saleItemInsert = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement temp = conn.prepareStatement(saleItemInsert);
            for (Item item : cart.getItems()) {
                temp.setInt(1, user.getId());
                temp.setString(2, item.getMovieId());
                temp.setDate(3, Date.valueOf(java.time.LocalDate.now()));
                temp.setInt(4, item.getQuantity());
                temp.addBatch();
            }
            temp.executeBatch();
            conn.commit();
            session.removeAttribute("shoppingCart");
            out.println("<html><head><title>Order Confirmation</title></head><body>");
            out.println("<h1>Order Confirmation</h1>");
            out.println("<p>Your order has been placed successfully!</p>");
            out.println("<p>Total Price: $" + String.format("%.2f", cart.getTotalPrice()) + "</p>");
            out.println("<a href='index.html'>Continue Shopping</a>");
            out.println("</body></html>");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            request.setAttribute("errorMessage", "An error occurred while processing your order. Please refresh the page.");
            //TODO: Handle purchase error
            return;
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }

    }
}