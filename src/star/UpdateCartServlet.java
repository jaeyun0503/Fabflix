package star;

import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

// Declaring a WebServlet called UpdateCartServlet, which maps to url "/api/updatecart"
@WebServlet(name = "UpdateCartServlet", urlPatterns = "/api/updatecart")
public class UpdateCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/ReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        String movieId = request.getParameter("movieId");
        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("shoppingCart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("shoppingCart", cart);
        }
        int currentQty = 0;
        for (Item item : cart.getItems()) {
            if (item.getMovieId().equals(movieId)) {
                currentQty = item.getQuantity();
                break;
            }
        }
        if ("increase".equalsIgnoreCase(action)) {
            cart.updateItem(movieId, currentQty + 1);
        } else if ("decrease".equalsIgnoreCase(action)) {
            cart.updateItem(movieId, currentQty - 1);
        } else if ("delete".equalsIgnoreCase(action)) {
            cart.removeItem(movieId);
        }

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("message", "star.Cart updated");
        jsonObj.addProperty("totalPrice", cart.getTotalPrice());

        out.write(jsonObj.toString());
    }
}