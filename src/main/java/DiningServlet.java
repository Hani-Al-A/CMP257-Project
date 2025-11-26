

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
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;


/**
 * Servlet implementation class DiningServlet
 */
public class DiningServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DiningServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		HttpSession session = request.getSession(false); // get session, don't create new one
		
	    String name = "";
	    int user_id = -1;
	    String email = "";
	    boolean isAdmin = false;
	    
	    String htmlTemplate = loadHtmlTemplate();
		String welcomeMessage = "";
		
	    
	    if (session != null && session.getAttribute("user") != null) {
	        name = (String) session.getAttribute("user");
	        user_id = (int) session.getAttribute("userId");
	        email = (String) session.getAttribute("email");
	        isAdmin = (boolean) session.getAttribute("isAdmin");
	        
	        htmlTemplate = htmlTemplate
	        		.replace("{{login_link}}", "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>"); //show a logout if logged in
	        
	    } else {
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login'>Login</a>"); // show login if not logged in
	    }
	    
	    
	    if (isAdmin) {
	    	htmlTemplate = htmlTemplate.replace("{{add_restaurant_button}}", 
	    			"<button type=\"button\" class=\"btn btn-primary\" data-bs-toggle=\"modal\" data-bs-target=\"#addRestaurantModal\">\n"
	    			+ "      			+ Add New Restaurant\n"
	    			+ "      		</button>")
	        		.replace("{{add_restaurant_form}}", getAddForm());

	    	
	    } else {
	    	htmlTemplate = htmlTemplate.replace("{{add_restaurant_button}}", "")
	    			.replace("{{add_restaurant_form}}", ""); // only showing the add button and form if the user is an admin
	    }
					
			StringBuilder allCardsHtml = new StringBuilder();
			
			
			try (Connection conn = DBConnection.getConnection();
				     Statement stmt = conn.createStatement();
				     ResultSet rs = stmt.executeQuery("SELECT * FROM restaurants")) { 
				
				int i = 0;
				while(rs.next()) {
					i++;
					int id = rs.getInt("restaurant_id");
					String rest_name = rs.getString("name");
					String desc = rs.getString("description");
					String img = rs.getString("image_url");
					String cuisine = rs.getString("cuisine_type");
					
					// Get the tags string and split it into an array
					String rawTags = rs.getString("tags");
					String[] tagsArray = (rawTags != null) ? rawTags.split(",") : new String[0];
					
					String cardHtml = getRestaurantCard(id, rest_name, desc, img, cuisine, tagsArray);
					
					//to make alternating alignment for restaurant cards
					if(i%2 == 0) {
						cardHtml = cardHtml.replace("gy-4 mb-5", "gy-4 mb-5 flex-md-row-reverse");
					}
					
					allCardsHtml.append(cardHtml);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				allCardsHtml.append("<p>Error loading restaurants: " + e.getMessage() + "</p>");
			}
			
			String fullHtml = htmlTemplate
					.replace("{{restaurant_cards}}", allCardsHtml);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println(fullHtml);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private String loadHtmlTemplate() throws IOException {
		// Obtain an InputStream for the HTML template file located in the web application
		try(InputStream is = getServletContext().getResourceAsStream("dining.html")){
			//Add logic to read input stream you could also use byte array ...
			// Create a Scanner to read the input stream,
			// Use "\\A" as the delimiter to read the entire input as a single token
			Scanner scanner = new Scanner(is). useDelimiter("\\A");
			// Check if there is any content to read
			// If there is, return the content as a String; otherwise, return an empty string
			return scanner.hasNext() ? scanner.next() : ""; // Read file contents
		}
		
	}
	
	private String getRestaurantCard(int id, String name, String description, String image_url, String cuisine, String[] tags) {
		StringBuilder tagsHtml = new StringBuilder();
		if (tags != null) {
			for(String tag : tags) {
				// We trim() to remove accidental spaces after commas
				tagsHtml.append("<span class='badge text-bg-primary me-2'>")
				        .append(tag.trim())
				        .append("</span>\n");
			}
		}
		String template = "<div class='row align-items-center gy-4 mb-5'>\n"
				+ "          <div class='col-md-6'>\n"
				+ "            <img src='"+ image_url +"' class='img-fluid rounded shadow' alt='"+ name +"'>\n"
				+ "          </div>\n"
				+ "          <div class='col-md-6'>\n"
				+ "            <h3 class='text-body-emphasis mb-2'>" + name + "</h3>\n"
				+ "            <p>"+ description +"</p>\n"
				+ "            <div class='mb-3'>\n"
				+ "              <span class='badge text-bg-primary me-2'>"+ cuisine +"</span>\n" // after this we need a for loop somehow to add all the tags
				+ 				tagsHtml
				+ "            </div>\n"
				+ "            <div class='d-flex gap-3'>\n"
				+ "              <a href='menu?id=" + id + "' class='btn btn-outline-primary'>View Menu</a>\n"
				+ "              <a href=\"reservation?id=" + id + "\" class=\"btn btn-primary\">Book Reservation</a>\n"
				+ "            </div>\n"
				+ "          </div>\n"
				+ "        </div>";
		return template;
	}
	
	private String getAddForm() {
		return "<div class=\"modal fade\" id=\"addRestaurantModal\" tabindex=\"-1\" aria-hidden=\"true\">\n"
				+ "      <div class=\"modal-dialog modal-dialog-centered\">\n"
				+ "        <div class=\"modal-content\">\n"
				+ "          <div class=\"modal-header bg-light\">\n"
				+ "            <h5 class=\"modal-title dark-header\">Add a New Restaurant</h5>\n"
				+ "            <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\n"
				+ "          </div>\n"
				+ "          <div class=\"modal-body\">\n"
				+ "            <form action=\"addRestaurant\" method=\"POST\">\n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Restaurant Name</label>\n"
				+ "                <input type=\"text\" name=\"name\" class=\"form-control\" required placeholder=\"e.g. Seaside Grill\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Cuisine Type</label>\n"
				+ "                <input type=\"text\" name=\"cuisine\" class=\"form-control\" required placeholder=\"e.g. Seafood, Italian\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Description</label>\n"
				+ "                <textarea name=\"description\" class=\"form-control\" rows=\"3\" required placeholder=\"Short description...\"></textarea>\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Tags</label>\n"
				+ "                <input type=\"text\" name=\"tags\" class=\"form-control\" placeholder=\"e.g. Lunch, Dinner, Outdoor Seating\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"mb-3\">\n"
				+ "                <label class=\"form-label text-muted small\">Image URL</label>\n"
				+ "                <input type=\"url\" name=\"image_url\" class=\"form-control\" required placeholder=\"https://example.com/image.jpg\">\n"
				+ "              </div>\n"
				+ "              \n"
				+ "              <div class=\"d-grid gap-2 mt-4\">\n"
				+ "                <button type=\"submit\" class=\"btn btn-primary\">Add Restaurant</button>\n"
				+ "              </div>\n"
				+ "            </form>\n"
				+ "          </div>\n"
				+ "        </div>\n"
				+ "      </div>\n"
				+ "    </div>";
	}

}
