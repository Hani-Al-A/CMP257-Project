

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

import com.mysql.cj.xdevapi.Statement;

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
			
			String htmlTemplate = loadHtmlTemplate();
			
			StringBuilder allCardsHtml = new StringBuilder();
			
			
			try (Connection conn = DBConnection.getConnection();
				     Statement stmt = conn.createStatement();
				     ResultSet rs = stmt.executeQuery("SELECT * FROM restaurants")) { 
				
				int i = 0;
				while(rs.next()) {
					i++;
					int id = rs.getInt("restaurant_id");
					String name = rs.getString("name");
					String desc = rs.getString("description");
					String img = rs.getString("image_url");
					String cuisine = rs.getString("cuisine_type");
					
					// Get the tags string and split it into an array
					String rawTags = rs.getString("tags");
					String[] tagsArray = (rawTags != null) ? rawTags.split(",") : new String[0];
					
					String cardHtml = getRestaurantCard(id, name, desc, img, cuisine, tagsArray);
					
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

}
