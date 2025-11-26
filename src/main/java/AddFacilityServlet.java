import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/addFacility")
public class AddFacilityServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin == null || !isAdmin) { 
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<script>\n"
                    + "alert('Access Denied: You must be an administrator to add facilities.');\n"
                    + "window.location.href = 'login';\n"
                    + "</script>");
            return; 
        }

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String timings = req.getParameter("timings");
        String image = req.getParameter("image_url");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO facilities (name, description, timings, image_url) VALUES (?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, timings);
            ps.setString(4, image);
            ps.executeUpdate();
            resp.sendRedirect("facilities"); // go back to the facilities page
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding facility: " + e.getMessage());
        }
    }
}
