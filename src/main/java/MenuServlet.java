
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public MenuServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(false); // get session, don't create new one
		
	    String name = "";
	    int user_id = -1;
	    String email = "";
	    boolean isAdmin = false;
	    
	    String idParam = request.getParameter("id");
	    
	    int restaurantId = Integer.parseInt(idParam);
	    
	    String htmlTemplate = loadHtmlTemplate();
		String welcomeMessage = "";
	    
	    if (session != null && session.getAttribute("user") != null) {
	        name = (String) session.getAttribute("user");
	        user_id = (int) session.getAttribute("userId");
	        email = (String) session.getAttribute("email");
	        isAdmin = (boolean) session.getAttribute("isAdmin");
	        
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>"); //show a logout if logged in
	    } else {
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login'>Login</a>"); // show login if not logged in
	    }
	    
	    if (isAdmin) {
	    	htmlTemplate = htmlTemplate
	    			.replace("{{add_item_button}}", "<button type=\"button\" class=\"btn btn-primary\" data-bs-toggle=\"modal\" data-bs-target=\"#addMenuItemModal\">\n"
		    			+ "                + Add Menu Item\n"
		    			+ "             </button>")
	    			.replace("{{add_item_form}}", getAddItemHtml())
	    			.replace("{{restaurant_id}}", Integer.toString(restaurantId));
	    			
	    } else {
	    	htmlTemplate = htmlTemplate
	    			.replace("{{add_item_button}}", "")
	    			.replace("{{add_item_form}}", "");
	    }
		
		
		
		if(idParam == null || idParam.isEmpty()) {
			response.sendRedirect("dining.html");
			return;
		}
		
		
		
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
			
			// Get Menu Items
			String sqlMenu = "SELECT * FROM menu_items WHERE restaurant_id = " + restaurantId;
			ResultSet rsMenu = stmt.executeQuery(sqlMenu);
			
			while(rsMenu.next()) {
				String itemName = rsMenu.getString("item_name");
				String itemDesc = rsMenu.getString("description");
				double price = rsMenu.getDouble("price");
				String category = rsMenu.getString("category");
				int menuId = rsMenu.getInt("menu_id");
				
				menuHtml.append(getMenuItemCard(menuId, restaurantId, itemName, itemDesc, price, category, isAdmin));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			menuHtml.append("<p>Error loading menu: " + e.getMessage() + "</p>");
		}
		
		String finalHtml = htmlTemplate
				.replace("{{restaurant_name}}", restaurantName)
				.replace("{{menu_list}}", menuHtml.toString());
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print(finalHtml);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("update".equals(action)) {
            updateMenuItem(request, response);
        } else {
            doGet(request, response);
        }
	}
	
	private void updateMenuItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        boolean isAdmin = (session != null && session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin"));

        if (!isAdmin) {
            response.sendRedirect("login"); 
            return;
        }

        int menuId = Integer.parseInt(request.getParameter("menu_id"));
        int restaurantId = Integer.parseInt(request.getParameter("restaurant_id"));
        String itemName = request.getParameter("item_name");
        String description = request.getParameter("description");
        double price = Double.parseDouble(request.getParameter("price"));
        String category = request.getParameter("category");

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE menu_items SET item_name = ?, description = ?, price = ?, category = ? WHERE menu_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, itemName);
                ps.setString(2, description);
                ps.setDouble(3, price);
                ps.setString(4, category);
                ps.setInt(5, menuId);
                
                ps.executeUpdate();
            }
            response.sendRedirect("menu?id=" + restaurantId); 

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Error updating menu item: " + e.getMessage());
        }
    }
	
	private String loadHtmlTemplate() throws IOException {
		try(InputStream is = getServletContext().getResourceAsStream("menu.html")){
			if(is == null) return "<h1>Error: menu.html not found</h1>";
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : ""; 
		}
	}
	
	
	private String getMenuItemCard(int menuId, int restaurantId, String name, String desc, double price, String category, boolean isAdmin) {
		
		
		String adminControls = "";
		if(isAdmin) { // only shows controls like delete and edit buttons for admins
			adminControls = "<div class='mt-2 border-top pt-2'>" +
                    "<button class='btn btn-sm btn-outline-primary me-2' " +
                    "  data-bs-toggle='modal' " +
                    "  data-bs-target='#editMenuItemModal' " +
                    "  data-id='" + menuId + "' " +
                    "  data-restaurant='" + restaurantId + "' " +
                    "  data-name='" + name + "' " +
                    "  data-price='" + price + "' " +
                    "  data-cat='" + category + "' " +
                    "  data-desc='" + desc + "'>" +
                    "Edit</button>" +
                    "<a href='deleteMenuItem?id=" + menuId + "&restaurant_id=" + restaurantId + "' class='btn btn-sm btn-outline-danger' onclick=\"return confirm('Delete " + name + "?');\">Delete</a>" +
                    "</div>";
		}
		
		return "<div class=\"col-md-6 col-lg-4\">\n"
				+ "    <div class=\"card h-100 shadow-sm\">\n"
				+ "        <div class=\"card-body\">\n"
				+ "            <div class=\"d-flex justify-content-between align-items-start\">\n"
				+ "                <h5 class=\"text-body-emphasis mb-1\">" + name + "</h5>\n"
				+ "                <span class=\"badge bg-secondary\">" + category + "</span>\n"
				+ "            </div>\n"
				+ "            <h6 class=\"text-success fw-bold my-2\">AED " + price + "</h6>\n"
				+ "            <p class=\"card-text text-muted\">" + desc + "</p>\n"
				+              adminControls // delete button if admin
				+ "        </div>\n"
				+ "    </div>\n"
				+ "</div>";
	}
	
	private String getAddItemHtml() {
		return "<div class=\"modal fade\" id=\"addMenuItemModal\" tabindex=\"-1\" aria-hidden=\"true\">\n"
				+ "      <div class=\"modal-dialog modal-dialog-centered\">\n"
				+ "        <div class=\"modal-content\">\n"
				+ "          <div class=\"modal-header bg-light\">\n"
				+ "            <h5 class=\"modal-title dark-header\">Add New Item to {{restaurant_name}}</h5>\n"
				+ "            <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\n"
				+ "          </div>\n"
				+ "          <div class=\"modal-body\">\n"
				+ "            <form action=\"/addMenuItem\" method=\"POST\">\n"
				+ "              <input type=\"hidden\" name=\"restaurant_id\" value=\"{{restaurant_id}}\">\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Item Name</label>\n"
				+ "                <input type=\"text\" name=\"item_name\" class=\"form-control\" required placeholder=\"e.g. Truffle Fries\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Category</label>\n"
				+ "                <select name=\"category\" class=\"form-select\">\n"
				+ "                    <option value=\"Starter\">Starter</option>\n"
				+ "                    <option value=\"Main\">Main</option>\n"
				+ "                    <option value=\"Dessert\">Dessert</option>\n"
				+ "                    <option value=\"Drink\">Drink</option>\n"
				+ "                    <option value=\"Pass\">Pass (Buffet)</option>\n"
				+ "                </select>\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Price (AED)</label>\n"
				+ "                <input type=\"number\" name=\"price\" class=\"form-control\" required step=\"0.01\" min=\"0\" placeholder=\"0.00\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Description</label>\n"
				+ "                <textarea name=\"description\" class=\"form-control\" rows=\"2\" required placeholder=\"Ingredients, allergens...\"></textarea>\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"d-grid gap-2 mt-4\">\n"
				+ "                <button type=\"submit\" class=\"btn btn-primary\">Add Item</button>\n"
				+ "              </div>\n"
				+ "            </form>\n"
				+ "          </div>\n"
				+ "        </div>\n"
				+ "      </div>\n"
				+ "    </div>";
	}
}