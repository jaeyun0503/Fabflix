package star;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            Map<String, List<Map<String, String>>> metadataMap = new HashMap<>();

            ResultSet tables = metaData.getTables("moviedb", null, null, new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                // List to hold column metadata for the current table
                List<Map<String, String>> columnsList = new ArrayList<>();

                // Get columns for the current table
                ResultSet columns = metaData.getColumns("moviedb", null, tableName, null);
                while (columns.next()) {
                    Map<String, String> columnMap = new HashMap<>();
                    columnMap.put("attr", columns.getString("COLUMN_NAME"));
                    columnMap.put("type", columns.getString("TYPE_NAME") + "(" + columns.getInt("COLUMN_SIZE") + ")");
                    columnsList.add(columnMap);
                }
                metadataMap.put(tableName, columnsList);
            }
            String jsonMetadata = toJson(metadataMap);
            // Write the JSON response to the output
            out.write(jsonMetadata);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }


    private String toJson(Map<String, List<Map<String, String>>> metadataMap) {
        StringBuilder json = new StringBuilder("{");
        boolean firstTable = true;

        for (Map.Entry<String, List<Map<String, String>>> entry : metadataMap.entrySet()) {
            if (!firstTable) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":[");
            boolean firstColumn = true;
            for (Map<String, String> column : entry.getValue()) {
                if (!firstColumn) {
                    json.append(",");
                }
                json.append("{");
                boolean firstAttribute = true;
                for (Map.Entry<String, String> attribute : column.entrySet()) {
                    if (!firstAttribute) {
                        json.append(",");
                    }
                    json.append("\"").append(attribute.getKey()).append("\":\"").append(attribute.getValue()).append("\"");
                    firstAttribute = false;
                }
                json.append("}");
                firstColumn = false;
            }
            json.append("]");
            firstTable = false;
        }
        json.append("}");
        return json.toString();
    }

}
