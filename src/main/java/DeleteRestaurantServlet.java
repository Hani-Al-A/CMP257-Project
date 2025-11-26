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


@WebServlet("/deleteRestaurant")
public class DeleteRestaurantServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public DeleteRestaurantServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        if (isAdmin == null || !isAdmin) { 
        	response.setContentType("text/html");
        	PrintWriter out = response.getWriter();
        	out.println("<script>");
        	out.println("alert('Access Denied: You must be an admin to delete restaurants.');");
        	out.println("window.location.href = 'dining';"); 
        	out.println("</script>");
        	return; 
        }
        
        String idParam = request.getParameter("id");
        if(idParam == null) { // if user enters /deleteRestaurant with no id
        	response.sendRedirect("dining");
        	return;
        }
        
        int restaurantId = Integer.parseInt(idParam);
        
        try (Connection conn = DBConnection.getConnection()) {
        	String sql = "DELETE FROM restaurants WHERE restaurant_id = ?";
        	try(PreparedStatement ps = conn.prepareStatement(sql)) {
        		ps.setInt(1, restaurantId);
        		ps.executeUpdate();
        	}
        	response.sendRedirect("dining");
        	
        } catch (SQLException e) {
        	e.printStackTrace();
        	response.getWriter().println("Error deleting restaurant: " + e.getMessage());
        }
	}
}