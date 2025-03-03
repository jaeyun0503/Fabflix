import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called Genre, which maps to url "/api/genre"
@WebServlet(name = "GenreServlet", urlPatterns = "/api/genre")
public class GenreServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/ReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String genreId = request.getParameter("id");
        int page = request.getParameter("page") != null ? Integer.parseInt(request.getParameter("page")) : 1;
        int resultsPerPage = request.getParameter("resultsPerPage") != null ? Integer.parseInt(request.getParameter("resultsPerPage")) : 25;
        int offset = (page - 1) * resultsPerPage;
        String sortBy = request.getParameter("sortBy");
        if (sortBy == null) {
            sortBy = "ratinghltitlelh";
        }
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + genreId);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String order = "ORDER BY rating DESC, m.title ASC ";
        if ("titlelhratinghl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title ASC, rating DESC ";
        } else if ("titlehlratinghl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title DESC, rating DESC ";
        } else if ("ratinglhtitlelh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating ASC, m.title ASC ";
        } else if ("ratinglhtitlehl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating ASC, m.title DESC ";
        }else if ("ratinghltitlehl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating DESC, m.title DESC ";
        }else if ("titlelhratinglh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title ASC, rating ASC ";
        }else if ("titlehlratinglh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title DESC, rating ASC ";
        }
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT g.name AS genre_name, m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, m.price " +
                    "FROM genres g " +
                    "JOIN genres_in_movies gm ON g.id = gm.genreId " +
                    "JOIN movies m ON gm.movieId = m.id " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE g.id = ? " +
                    order +
                    "LIMIT ? OFFSET ?;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, genreId);
            statement.setInt(2, resultsPerPage);
            statement.setInt(3, offset);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            String genreName = "";

            // Iterate through each row of rs
            while (rs.next()) {
                if (genreName.isEmpty()) {
                    genreName = rs.getString("genre_name");
                }

                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");
                String moviePrice = rs.getString("price");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("movie_price", moviePrice);

                jsonArray.add(jsonObject);
            }

            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonObject movieObj = jsonArray.get(i).getAsJsonObject();
                String movieId = movieObj.get("movie_id").getAsString();

                // Functions Class
                JsonObject stars = Functions.retrieveMovieStars(movieId, 3, response, out, conn);
                JsonObject genres = Functions.retrieveMovieGenres(movieId, 3, response, out, conn);
                movieObj.add("movie_stars", stars);
                movieObj.add("movie_genres", genres);
            }

            rs.close();
            statement.close();

            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("genre_name", genreName);
            responseObj.add("movies", jsonArray);

            String countQuery = "SELECT COUNT(*) as total_count "
                    + "FROM genres g "
                    + "JOIN genres_in_movies gm ON g.id = gm.genreId "
                    + "JOIN movies m ON gm.movieId = m.id "
                    + "WHERE g.id = ?";

            try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                countStmt.setString(1, genreId);
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int totalMovies = countRs.getInt("total_count");
                    responseObj.addProperty("total_movies", totalMovies);

                    // Also calculate total_pages:
                    int totalPages = (int) Math.ceil((double) totalMovies / resultsPerPage);
                    responseObj.addProperty("total_pages", totalPages);
                }
                countRs.close();
            }

            // Write JSON string to output
            out.write(responseObj.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
