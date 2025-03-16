package star;

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

// Declaring a WebServlet called star.AddCartServlet, which maps to url "/api/addcart"
@WebServlet(name = "star.AddCartServlet", urlPatterns = "/api/addcart")
public class AddCartServlet extends HttpServlet {
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
        String title = request.getParameter("title");
        String priceStr = request.getParameter("price");
        double price = 0.0;
        PrintWriter out = response.getWriter();

        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            price = 0.0;
        }

        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("shoppingCart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("shoppingCart", cart);
        }
        cart.addItem(movieId, title, price);

        response.setContentType("text/plain");
        out.write("star.Item added to cart");
        response.setStatus(200);
    }
}