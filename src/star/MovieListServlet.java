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

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called star.MovieListServlet, which maps to url "/api/movielist"
@WebServlet(name = "star.MovieListServlet", urlPatterns = "/api/movielist")
public class MovieListServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        String title = request.getParameter("title");
        if (title == null) {title = "";}
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String genre = request.getParameter("genre");
        String letter = request.getParameter("letter");
        //Check browsing or searching
        boolean isSearch = (title != null && !title.trim().isEmpty()) ||
                (year != null && !year.trim().isEmpty()) ||
                (director != null && !director.trim().isEmpty()) ||
                (star != null && !star.trim().isEmpty());

        boolean isBrowse = (genre != null && !genre.trim().isEmpty()) ||
                (letter != null && !letter.trim().isEmpty());

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User("a@email.com");
            user.setPage(1);
            user.setResultsPerPage(25);
            user.setSortBy("ratinghltitlelh");
        }

        if (isSearch) {
            if (title != null) {
                user.setTitle(title);
            } else
                user.setTitle(null);
            if (year != null) {
                user.setYear(year);
            } else {
                user.setYear(null);
            }
            if (director != null) {
                user.setDirector(director);
            } else {
                user.setDirector(null);
            }
            if (star != null) {
                user.setStar(star);
            } else {
                user.setStar(null);
            }
            user.setGenre(null);
            user.setLetter(null);
        } else if (isBrowse) {
            if (genre != null && !genre.trim().isEmpty()) {
                letter = null;
            } else {
                genre = null;
            }
            user.setTitle(null);
            user.setYear(null);
            user.setDirector(null);
            user.setStar(null);
            user.setGenre(genre);
            user.setLetter(letter);
        }

        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            user.setPage(Integer.parseInt(pageParam));
        }

        String resultsPer = request.getParameter("resultsPerPage");
        if (resultsPer != null && !resultsPer.isEmpty())  {
            user.setResultsPerPage(Integer.parseInt(resultsPer));
        }

        String sortBy = request.getParameter("sortBy");
        if (sortBy != null && !sortBy.isEmpty()) {
            user.setSortBy(sortBy);
        }
        session.setAttribute("user", user);

        title = user.getTitle();
        if (title == null) {title = "";}
        year = user.getYear();
        director = user.getDirector();
        star = user.getStar();
        int resultsPerPage = user.getResultsPerPage();
        sortBy = user.getSortBy();
        int page = user.getPage();
        int offset = (page - 1) * resultsPerPage;
        genre = user.getGenre();
        letter = user.getLetter();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        JsonObject responseObj = new JsonObject();
        String order = "ORDER BY rating DESC, m.title ASC ";
        if ("titlelhratinghl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title ASC, rating DESC ";
        } else if ("titlehlratinghl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title DESC, rating DESC ";
        } else if ("ratinglhtitlelh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating ASC, m.title ASC ";
        } else if ("ratinglhtitlehl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating ASC, m.title DESC ";
        } else if ("ratinghltitlehl".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY rating DESC, m.title DESC ";
        } else if ("titlelhratinglh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title ASC, rating ASC ";
        } else if ("titlehlratinglh".equalsIgnoreCase(sortBy)) {
            order = "ORDER BY m.title DESC, rating ASC ";
        }
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT SQL_CALC_FOUND_ROWS DISTINCT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, m.price " +
                            "FROM movies AS m " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                            "LEFT JOIN stars s on sim.starId = s.id " +
                            "WHERE true ");   // Set to 0 if rating is null

            String temp = "";
            int k = title.length() < 3 ? 0 : title.length() < 4 ? 1 : title.length() < 7 ? 2 : 3;
            if (title != null && !title.trim().isEmpty()) {
                String [] tokens = title.split(" ");
                for (String word : tokens) {
                    temp += "+" + word + "* ";
                }
                query.append(" AND (MATCH (m.title) AGAINST (? IN BOOLEAN MODE) ");
                query.append("OR ed(lower(title), '").append(title.toLowerCase()).append("') <= ");
                query.append(k);
                query.append(") ");
            } else {
                temp = "%";
                query.append(" AND (m.title LIKE ? OR ed(lower(title), '");
                query.append(title.toLowerCase()).append("') <= ");
                query.append(k);
                query.append(") ");
            }

            if (year != null && !year.trim().isEmpty()) {
                query.append(" AND m.year = ? ");
            }
            if (director != null && !director.trim().isEmpty()) {
                query.append("AND m.director LIKE ? ");
            }
            if (star != null && !star.trim().isEmpty()) {
                query.append("AND s.name LIKE ? ");
            }
            if (genre != null && !genre.trim().isEmpty()) {
                query.append("AND m.id IN (SELECT mg.movieId FROM genres_in_movies mg " +
                        "JOIN genres g ON mg.genreId = g.id WHERE g.name = ?) ");
            }
            if (letter != null && !letter.trim().isEmpty()) {
                if (letter.equals("*")) {
                    query.append("AND m.title REGEXP '^[^A-Za-z0-9]' ");
                } else {
                    query.append("AND LOWER(m.title) LIKE ? ");
                }
            }

            query.append(order + "LIMIT ? OFFSET ?");

            PreparedStatement statement = conn.prepareStatement(query.toString());
            int pos = 1;

//            if (title != null && !title.isEmpty())
            statement.setString(pos++, temp);

            if (year != null && !year.isEmpty())
                try {
                    statement.setInt(pos++, Integer.parseInt(year));
                } catch (NumberFormatException e) {
                    statement.setInt(pos++, 0);
                }

            if (director != null && !director.isEmpty())
                statement.setString(pos++, "%" + director + "%");

            if (star != null && !star.isEmpty())
                statement.setString(pos++, "%" + star + "%");

            if (genre != null && !genre.trim().isEmpty()) {
                statement.setString(pos++, genre.trim());
            }

            if (letter != null && !letter.trim().isEmpty() && !letter.equals("*")) {
                statement.setString(pos++, letter.toLowerCase() + "%");
            }

            statement.setInt(pos++, resultsPerPage);
            statement.setInt(pos++, offset);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray moviesArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                String movie_price = rs.getString("price");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_price", movie_price);

                moviesArray.add(jsonObject);
            }

            rs.close();
            statement.close();

            for (int i = 0; i < moviesArray.size(); ++i) {
                JsonObject movieObj = moviesArray.get(i).getAsJsonObject();
                String movieId = movieObj.get("movie_id").getAsString();

                // Functions Class
                JsonObject stars = Functions.retrieveMovieStars(movieId, 3, response, out, conn);
                JsonObject genres = Functions.retrieveMovieGenres(movieId, 3, response, out, conn);
                movieObj.add("movie_stars", stars);
                movieObj.add("movie_genres", genres);
            }

            String countQuery = "SELECT FOUND_ROWS() AS total_count";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);

//
//            String countQuery =
//                    "SELECT COUNT(DISTINCT m.id) as total_count " +
//                            "FROM movies m " +
//                            "LEFT JOIN ratings r ON m.id = r.movieId " +
//                            "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
//                            "LEFT JOIN stars s ON sim.starId = s.id " +
//                            "WHERE 1=1 ";
//
//            if (title != null && !title.trim().isEmpty()) {
//                countQuery += "AND m.title LIKE ? ";
//            }
//            if (year != null && !year.trim().isEmpty()) {
//                countQuery += "AND m.year = ? ";
//            }
//            if (director != null && !director.trim().isEmpty()) {
//                countQuery += "AND m.director LIKE ? ";
//            }
//            if (star != null && !star.trim().isEmpty()) {
//                countQuery += "AND s.name LIKE ? ";
//            }
//            if (genre != null && !genre.trim().isEmpty()) {
//                countQuery += "AND m.id IN (SELECT mg.movieId FROM genres_in_movies mg " +
//                        "JOIN genres g ON mg.genreId = g.id WHERE g.name = ?) ";
//            }
//            if (letter != null && !letter.trim().isEmpty()) {
//                if (letter.equals("*")) {
//                    countQuery += "AND m.title REGEXP '^[^A-Za-z0-9]' ";
//                } else {
//                    countQuery += "AND LOWER(m.title) LIKE ? ";
//                }
//            }
//
//            PreparedStatement countStmt = conn.prepareStatement(countQuery);
//            pos = 1;
//            if (title != null && !title.trim().isEmpty()) {
//                countStmt.setString(pos++, "%" + title.trim() + "%");
//            }
//            if (year != null && !year.trim().isEmpty()) {
//                try {
//                    countStmt.setInt(pos++, Integer.parseInt(year.trim()));
//                } catch (NumberFormatException e) {
//                    countStmt.setInt(pos++, 0);
//                }
//            }
//            if (director != null && !director.trim().isEmpty()) {
//                countStmt.setString(pos++, "%" + director.trim() + "%");
//            }
//            if (star != null && !star.trim().isEmpty()) {
//                countStmt.setString(pos++, "%" + star.trim() + "%");
//            }
//            if (genre != null && !genre.trim().isEmpty()) {
//                countStmt.setString(pos++, genre.trim());
//            }
//            if (letter != null && !letter.trim().isEmpty() && !letter.equals("*")) {
//                countStmt.setString(pos++, letter.toLowerCase() + "%");
//            }
//
            ResultSet countRs = countStmt.executeQuery();
            int totalMovies = 0;
            if (countRs.next()) {
                totalMovies = countRs.getInt("total_count");
            }
            countRs.close();
            countStmt.close();
            int totalPages = (int) Math.ceil((double) totalMovies / resultsPerPage);

            responseObj.add("movies", moviesArray);
            responseObj.addProperty("total_movies", totalMovies);
            responseObj.addProperty("total_pages", totalPages);
            responseObj.addProperty("currentPage", page);
            responseObj.addProperty("currentSortBy", sortBy);

            out.write(responseObj.toString());
            // Log to localhost log
            request.getServletContext().log("getting " + responseObj.size() + " results");
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);

        } finally {
            out.close();
        }
    }
}