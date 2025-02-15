import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.http.HttpServletResponse;

import javax.xml.transform.Result;


public class Functions {   // Miscellaneous Functions
    public static JsonObject retrieveMovieStars(String movieId, int limit, HttpServletResponse response, PrintWriter out, Connection conn) throws SQLException {
        JsonObject result = new JsonObject();
        String query;
        PreparedStatement statement;
        ResultSet rs;

        try {
            if (conn != null && !conn.isClosed()) {
                if (limit > 0) {
                    query = "SELECT s.name, s.id FROM stars s, stars_in_movies m WHERE s.id = m.starId AND m.movieId = ? LIMIT ?";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                    statement.setInt(2, limit);
                } else {
                    query = "SELECT s.name, s.id FROM stars s, stars_in_movies m WHERE s.id = m.starId AND m.movieId = ?";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                }
                rs = statement.executeQuery();

                JsonArray stars = new JsonArray();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String id = rs.getString("id");
                    stars.add(name);
                    stars.add(id);
                }
                result.add("stars", stars);
                rs.close();
            }
        } catch (Exception e) {
            JsonObject js = new JsonObject();
            js.addProperty("errorMessage", e.getMessage());
            out.write(js.toString());
            response.setStatus(500);
        }
        return result;
    }

    public static JsonObject retrieveMovieGenres(String movieId, int limit, HttpServletResponse response, PrintWriter out, Connection conn) throws SQLException {
        JsonObject result = new JsonObject();
        String query;
        PreparedStatement statement;
        ResultSet rs;

        try {
            if (conn != null && !conn.isClosed()) {
                if (limit > 0) {
                    query = "SELECT g.id, g.name FROM genres g, genres_in_movies m WHERE g.id = m.genreId AND m.movieId = ? ORDER BY g.name ASC LIMIT ?";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                    statement.setInt(2, limit);
                } else {
                    query = "SELECT g.id, g.name FROM genres g, genres_in_movies m WHERE g.id = m.genreId AND m.movieId = ? ORDER BY g.name";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, movieId);
                }
                rs = statement.executeQuery();

                JsonArray genres = new JsonArray();
                while (rs.next()) {
                    String genreName = rs.getString("name");
                    String genreId = rs.getString("id");
                    genres.add(genreName);
                    genres.add(genreId);
                }

                result.add("genres", genres);
                rs.close();
            }
        } catch (Exception e) {
            JsonObject js = new JsonObject();
            js.addProperty("errorMessage", e.getMessage());
            out.write(js.toString());
            response.setStatus(500);
        }

        return result;
    }

    public static JsonObject retrieveSingleMovieStars(String movieId, HttpServletResponse response, PrintWriter out, Connection conn) throws SQLException {
        JsonObject result = new JsonObject();
        String query;
        PreparedStatement statement;
        ResultSet rs;

        try {
            if (conn != null && !conn.isClosed()) {
                query = "SELECT s.name, s.id, COUNT(sim.movieId) AS movie_count " +
                        "FROM stars s " +
                        "JOIN stars_in_movies sim ON s.id = sim.starId " +
                        "WHERE s.id IN ( " +
                        "    SELECT starId FROM stars_in_movies WHERE movieId = ? " +
                        ") " +
                        "GROUP BY s.id, s.name " +
                        "ORDER BY movie_count DESC, s.name ASC;";
                statement = conn.prepareStatement(query);
                statement.setString(1, movieId);
                rs = statement.executeQuery();

                JsonArray stars = new JsonArray();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String id = rs.getString("id");
                    stars.add(name);
                    stars.add(id);
                }
                result.add("stars", stars);
                rs.close();
            }
        } catch (Exception e) {
            JsonObject js = new JsonObject();
            js.addProperty("errorMessage", e.getMessage());
            out.write(js.toString());
            response.setStatus(500);
        }
        return result;
    }
}
