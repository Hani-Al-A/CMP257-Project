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

@WebServlet(urlPatterns = { "/facilities", "/facilities.html" })
public class FacilitiesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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

        String htmlTemplate = loadHtmlTemplate();
        StringBuilder facilityCardsHtml = new StringBuilder();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM facilities ORDER BY facility_id")) {

            int lastFacilityId = -1;
            StringBuilder subCardsHtml = new StringBuilder();
            String facilityName = "";
            String facilityDescription = "";
            String facilityImage = "";
            String facilityTimings = "";
            String facilityAmenities = "";

            while (rs.next()) {
                int id = rs.getInt("facility_id");

                if (id != lastFacilityId && lastFacilityId != -1) {
                    facilityCardsHtml.append(getFacilityCard(facilityName, facilityDescription,
                            facilityImage, subCardsHtml.toString(), facilityTimings, facilityAmenities));
                    subCardsHtml = new StringBuilder(); 
                }

                facilityName = rs.getString("name");
                facilityDescription = rs.getString("description");
                facilityImage = rs.getString("image_url");
                facilityTimings = rs.getString("timings");
                facilityAmenities = rs.getString("amenities");


                lastFacilityId = id;
            }

            if (lastFacilityId != -1) {
                facilityCardsHtml.append(getFacilityCard(facilityName, facilityDescription,
                        facilityImage, subCardsHtml.toString(), facilityTimings, facilityAmenities));
            }

        } catch (Exception e) {
            e.printStackTrace();
            facilityCardsHtml.append("<p>Error loading facilities: ").append(e.getMessage()).append("</p>");
        }

        String fullHtml = htmlTemplate.replace("{{facility_cards}}", facilityCardsHtml.toString());

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(fullHtml);
    }

    private String loadHtmlTemplate() throws IOException {
        try (InputStream is = getServletContext().getResourceAsStream("/facilities.html")) {
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String getFacilityCard(String name, String description, String imageUrl,
                                   String subCardsHtml, String timings, String amenities) {

        StringBuilder detailsHtml = new StringBuilder();
        if ((timings != null && !timings.isEmpty()) || (amenities != null && !amenities.isEmpty())) {
            detailsHtml.append("<div class='text-center mt-3'>")
                       .append("<button class='btn btn-primary toggle-info'>View Opening Timings & Amenities</button>")
                       .append("<div class='details' style='display: none; margin-top: 10px'>");

            if (timings != null && !timings.isEmpty()) {
                detailsHtml.append("<strong>Opening Timings:</strong><ul>");
                for (String t : timings.split(",")) {
                    detailsHtml.append("<li>").append(t).append("</li>");
                }
                detailsHtml.append("</ul>");
            }

            if (amenities != null && !amenities.isEmpty()) {
                detailsHtml.append("<strong>Amenities:</strong><ul>");
                for (String a : amenities.split(",")) {
                    detailsHtml.append("<li>").append(a).append("</li>");
                }
                detailsHtml.append("</ul>");
            }

            detailsHtml.append("</div></div>");
        }

        return "<div class='facility-card'>"
                + "<h2 class='dark-header'>" + name + "</h2>"
                + "<p>" + description + "</p>"
                + (subCardsHtml.isEmpty() ? "" : "<div class='sub-grid'>" + subCardsHtml + "</div>")
                + "<img src='" + imageUrl + "' alt='" + name + "' class='card-image full-width'/>"
                + detailsHtml
                + "</div>";
    }
}
