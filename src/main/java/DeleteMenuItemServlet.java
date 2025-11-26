
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


@WebServlet("/deleteMenuItem")
public class DeleteMenuItemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public DeleteMenuItemServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        String restaurantIdStr = request.getParameter("restaurant_id");
        String redirectUrl = (restaurantIdStr != null) ? "menu?id=" + restaurantIdStr : "dining";

        if (isAdmin == null || !isAdmin) { 
        	response.setContentType("text/html");
        	PrintWriter out = response.getWriter();
        	out.println("<script>");
        	out.println("alert('Access Denied: You must be an administrator to delete menu items.');");
        	out.println("window.location.href = '" + redirectUrl + "';"); 
        	out.println("</script>");
        	return; 
        }
        
        String idParam = request.getParameter("id");
        if(idParam == null) {
        	response.sendRedirect(redirectUrl);
        	return;
        }
        
        int menuId = Integer.parseInt(idParam);
        
        try (Connection conn = DBConnection.getConnection()) {
        	String sql = "DELETE FROM menu_items WHERE menu_id = ?";
        	try(PreparedStatement ps = conn.prepareStatement(sql)) {
        		ps.setInt(1, menuId);
        		ps.executeUpdate();
        	}
        	response.sendRedirect(redirectUrl);
        	
        } catch (SQLException e) {
        	e.printStackTrace();
        	response.getWriter().println("Error deleting menu item: " + e.getMessage());
        }
	}
}