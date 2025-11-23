
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public MenuServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 1. Get the Restaurant ID from the URL (menu?id=5)
		String idParam = request.getParameter("id");
		
		// Validation: If no ID is provided, send them back to dining page
		if(idParam == null || idParam.isEmpty()) {
			response.sendRedirect("dining.html");
			return;
		}
		
		int restaurantId = Integer.parseInt(idParam);
		
		String htmlTemplate = loadHtmlTemplate();
		String restaurantName = "Restaurant";
		StringBuilder menuHtml = new StringBuilder();
		
		try (Connection conn = DBConnection.getConnection();
		     Statement stmt = conn.createStatement()) {
			
			String sqlRest = "SELECT name FROM restaurants WHERE restaurant_id = " + restaurantId;
			ResultSet rsRest = stmt.executeQuery(sqlRest);
			
			if(rsRest.next()) {
				restaurantName = rsRest.getString("name");
			}
			rsRest.close();
			
			// QUERY 2: Get Menu Items
			String sqlMenu = "SELECT * FROM menu_items WHERE restaurant_id = " + restaurantId;
			ResultSet rsMenu = stmt.executeQuery(sqlMenu);
			
			while(rsMenu.next()) {
				String itemName = rsMenu.getString("item_name");
				String itemDesc = rsMenu.getString("description");
				double price = rsMenu.getDouble("price");
				String category = rsMenu.getString("category");
				
				menuHtml.append(getMenuItemCard(itemName, itemDesc, price, category));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			menuHtml.append("<p>Error loading menu: " + e.getMessage() + "</p>");
		}
		
		String finalHtml = htmlTemplate
				.replaceAll("{{restaurant_name}}", restaurantName)
				.replace("{{menu_list}}", menuHtml.toString());
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print(finalHtml);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private String loadHtmlTemplate() throws IOException {
		try(InputStream is = getServletContext().getResourceAsStream("menu.html")){
			if(is == null) return "<h1>Error: menu.html not found</h1>";
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : ""; 
		}
	}
	
	private String getMenuItemCard(String name, String desc, double price, String category) {
		return "<div class='col-md-6 col-lg-4'>\n"
				+ "    <div class='card h-100 shadow-sm'>\n"
				+ "        <div class='card-body'>\n"
				+ "            <div class='d-flex justify-content-between align-items-start'>\n"
				+ "                <h5 class='text-body-emphasis mb-1'>" + name + "</h3>\n"
				+ "                <span class='badge bg-secondary'>" + category + "</span>\n"
				+ "            </div>\n"
				+ "            <h6 class='text-success fw-bold my-2'>AED " + price + "</h6>\n"
				+ "            <p class='card-text text-muted'>" + desc + "</p>\n"
				+ "        </div>\n"
				+ "    </div>\n"
				+ "</div>";
	}
}