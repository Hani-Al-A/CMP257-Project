import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.*;

@WebServlet("/addFacility")
public class AddFacilityServlet extends HttpServlet {
boolean isAdmin = false;
	    
         
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            resp.sendRedirect("login.html");
            return;
        }

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String timings = req.getParameter("timings");
        String amenities = req.getParameter("amenities");
        String image = req.getParameter("image_url");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO facilities (name, description, timings, amenities, image_url) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, timings);
            ps.setString(4, amenities);
            ps.setString(5, image);

            ps.executeUpdate();
            resp.sendRedirect("admin-dashboard.html");

        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding facility: " + e.getMessage());
        }
    }
}
