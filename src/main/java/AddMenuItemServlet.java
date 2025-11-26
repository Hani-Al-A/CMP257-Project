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

@WebServlet("/addMenuItem")
public class AddMenuItemServlet extends HttpServlet {
   
         
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) { 
        	resp.setContentType("text/html");
        	PrintWriter out = resp.getWriter();
        	out.println("<script>\n"
        			+ "alert('Access Denied: You must be an administrator to add menu items.');\n"
        			+ "window.location.href = 'login';\n" // if the user (not admin) somehow manages to send a post request to add items, there is server side validation just in case
        			+ "</script>");
        	return; 
        }

        int restaurantId = Integer.parseInt(req.getParameter("restaurant_id"));
        String itemName = req.getParameter("item_name");
        String description = req.getParameter("description");
        double price = Double.parseDouble(req.getParameter("price"));
        String category = req.getParameter("category");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO menu_items (restaurant_id, item_name, description, price, category) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, restaurantId);
            ps.setString(2, itemName);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setString(5, category);
            ps.executeUpdate();
            resp.sendRedirect("menu?id=" + restaurantId);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding menu item: " + e.getMessage());
        }
    }
}
