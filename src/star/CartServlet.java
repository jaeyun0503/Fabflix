package star;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import star.Cart;
import star.Item;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

// Declaring a WebServlet called CartServlet, which maps to url "/api/cart"
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("shoppingCart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("shoppingCart", cart);
        }
        PrintWriter out = response.getWriter();

        JsonObject jsonObj = new JsonObject();
        JsonArray itemsArray = new JsonArray();

        for (Item item : cart.getItems()) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("movie_id", item.getMovieId());
            itemJson.addProperty("title", item.getTitle());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            itemsArray.add(itemJson);
        }

        jsonObj.add("items", itemsArray);
        jsonObj.addProperty("totalPrice", cart.getTotalPrice());

        out.write(jsonObj.toString());
        out.close();
    }
}