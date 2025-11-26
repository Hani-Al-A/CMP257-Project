import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/addRestaurant")
public class AddRestaurantServlet extends HttpServlet {
   
         
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) { resp.sendRedirect("login.html"); return; }

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String cuisineType = req.getParameter("cuisine");
        String tags = req.getParameter("tags");
        String image = req.getParameter("image_url");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO restaurants (name, description, cuisine_type, tags, image_url) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, cuisineType);
            ps.setString(4, tags);
            ps.setString(5, image);
            ps.executeUpdate();
            resp.sendRedirect("dining.html");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding restaurant: " + e.getMessage());
        }
    }
}
