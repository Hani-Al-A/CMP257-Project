import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = { "/rooms", "/rooms.html" })
public class RoomsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RoomsServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
	        
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>"); //show a logout if logged in
	    } else {
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login'>Login</a>"); // show login if not logged in
	    }
        
        StringBuilder allCardsHtml = new StringBuilder();

        String sql = "SELECT room_id, name, capacity, price_per_night, features, image_url "
                   + "FROM rooms WHERE is_available = 1";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("room_id");
                String room_name = rs.getString("name");
                int capacity = rs.getInt("capacity");
                double price = rs.getDouble("price_per_night");
                String features = rs.getString("features");
                String imageUrl = rs.getString("image_url");

                String cardHtml = getRoomCard(id, room_name, capacity, price, features, imageUrl);
                allCardsHtml.append(cardHtml);
            }

        } catch (Exception e) {
            e.printStackTrace();
            allCardsHtml.append("<p>Error loading rooms: ").append(e.getMessage()).append("</p>");
        }

        String fullHtml = htmlTemplate.replace("{{room_cards}}", allCardsHtml.toString());

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(fullHtml);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String loadHtmlTemplate() throws IOException {
        try (InputStream is = getServletContext().getResourceAsStream("rooms.html")) {
            if (is == null) {
                return "<h1>Error: rooms.html not found</h1>";
            }
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String getRoomCard(int id, String name, int capacity, double price,
            String features, String imageUrl) {

		if (features == null) {
		features = "";
		}
		
		String priceText = String.format("%.0f", price);
		
		StringBuilder featurePills = new StringBuilder();
		String[] featureList = features.split(",");
		
		for (String feature : featureList) {
		String cleanFeature = feature.trim();
		if (!cleanFeature.isEmpty()) {
		featurePills.append("<span class='badge bg-light text-dark border me-1'>")
		         .append(cleanFeature)
		         .append("</span>\n");
		}
		}
		
		String template =
		"<div class='col-12 col-md-6 col-lg-4'>\n"
		+ "  <div class='card h-100 shadow-sm room-card'>\n"
		+ "    <img src='" + imageUrl + "' class='card-img-top' alt='" + name + "'>\n"
		+ "    <div class='card-body d-flex flex-column'>\n"
		+ "      <h5 class='card-title dark-header'>" + name + "</h5>\n"
		
		+ "      <div class='d-flex flex-wrap gap-2 mb-3'>\n"
		+ "        <span class='badge bg-primary'>Sleeps " + capacity + "</span>\n"
		
		+          featurePills.toString()
		
		+ "      </div>\n"
		
		+ "      <div class='mt-auto d-flex justify-content-between align-items-end'>\n"
		+ "        <div>\n"
		+ "          <span class='fs-5 fw-semibold price'>AED " + priceText + "</span>\n"
		+ "          <small class='text-muted'>/ night</small>\n"
		+ "        </div>\n"
		+ "        <a href='/book?id="+ id +"' class='btn btn-success btn-sm book-btn'>Book</a>\n"
		+ "      </div>\n"
		+ "    </div>\n"
		+ "  </div>\n"
		+ "</div>\n";
		
		return template;
	}
}