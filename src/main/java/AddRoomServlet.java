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

@WebServlet("/addRoom")
public class AddRoomServlet extends HttpServlet {
     

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    
            throws IOException, ServletException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) { resp.sendRedirect("login.html"); return; }

        String name = req.getParameter("name");
        String description = req.getParameter("description");
        int capacity = Integer.parseInt(req.getParameter("capacity"));
        double price = Double.parseDouble(req.getParameter("price_per_night"));
        String features = req.getParameter("features");
        String image = req.getParameter("image_url");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO rooms (name, description, capacity, price_per_night, features, image_url) VALUES (?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, capacity);
            ps.setDouble(4, price);
            ps.setString(5, features);
            ps.setString(6, image);
            ps.executeUpdate();

            resp.sendRedirect("rooms.html");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding room: " + e.getMessage());
        }
    }
}
