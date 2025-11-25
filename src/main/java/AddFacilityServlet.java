
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/addFacility")
public class AddFacilityServlet extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/coastal_haven";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String timings = req.getParameter("timings");
        String amenities = req.getParameter("amenities");
        String image = req.getParameter("image_url");

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            String sql = "INSERT INTO facilities (name, description, timings, amenities, image_url) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, timings);
            ps.setString(4, amenities);
            ps.setString(5, image);

            ps.executeUpdate();

            resp.sendRedirect("admin-dashboard.html");

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding facility: " + e.getMessage());
        }
    }
}
