
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
import java.sql.Statement;
import java.util.Scanner;


@WebServlet(urlPatterns = {"/facilities", "/facilities.html"})
public class FacilitiesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        boolean isAdmin = false;

        String htmlTemplate = loadHtmlTemplate();
        
        if (session != null && session.getAttribute("user") != null) {
            isAdmin = (session.getAttribute("isAdmin") != null && (boolean) session.getAttribute("isAdmin"));
            htmlTemplate = htmlTemplate.replace("{{login_link}}",
                    "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>");
        } else {
            htmlTemplate = htmlTemplate.replace("{{login_link}}",
                    "<a class='nav-link mx-3' href='login'>Login</a>");
        }

        if (isAdmin) {
            htmlTemplate = htmlTemplate.replace("{{add_facility_button}}", getAddButton())
                                       .replace("{{add_facility_form}}", getAddForm());
        } else {
            htmlTemplate = htmlTemplate.replace("{{add_facility_button}}", "")
                                       .replace("{{add_facility_form}}", "");
        }

        StringBuilder allCardsHtml = new StringBuilder();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM facilities")) {

            while (rs.next()) {
                int id = rs.getInt("facility_id");
                String name = rs.getString("name");
                String desc = rs.getString("description");
                String timings = rs.getString("timings");
                String amenities = rs.getString("amenities");
                String img = rs.getString("image_url");

                allCardsHtml.append(getFacilityCard(id, name, desc, timings, amenities, img));
            }
        } catch (Exception e) {
            e.printStackTrace();
            allCardsHtml.append("<p>Error loading facilities: " + e.getMessage() + "</p>");
        }

        String fullHtml = htmlTemplate.replace("{{facility_cards}}", allCardsHtml.toString());

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(fullHtml);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Boolean isAdmin = (session != null) ? (Boolean) session.getAttribute("isAdmin") : false;

        if (isAdmin == null || !isAdmin) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('Access Denied: Only admins can add facilities'); window.location.href='facilities';</script>");
            return;
        }

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String timings = request.getParameter("timings");
        String amenities = request.getParameter("amenities");
        String imageUrl = request.getParameter("image_url");

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO facilities (name, description, timings, amenities, image_url) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, timings);
            ps.setString(4, amenities);
            ps.setString(5, imageUrl);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("facilities");
    }

    private String loadHtmlTemplate() throws IOException {
        try (InputStream is = getServletContext().getResourceAsStream("/facilities.html")) {
            if (is == null) return "<h1>Error: facilities.html not found</h1>";
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

private String getFacilityCard(int id, String name, String description, String timings, String amenities, String imageUrl) {
        
        StringBuilder detailsHtml = new StringBuilder();
        
        detailsHtml.append("<div class='mt-3'>")
                   .append("<button class='btn btn-primary toggle-info w-100'>View Timings & Amenities</button>")
                   .append("<div class='details p-3 border rounded bg-light mt-2' style='display: none;'>");

        if (timings != null && !timings.isEmpty()) {
            detailsHtml.append("<p class='mb-2'><strong>Opening Timings:</strong><br>")
                       .append(timings)
                       .append("</p>");
        }

        if (amenities != null && !amenities.isEmpty()) {
            detailsHtml.append("<strong>Amenities:</strong><br><div class='d-flex flex-wrap gap-1 mt-1'>");
            for (String item : amenities.split(",")) {
                detailsHtml.append("<span class='badge bg-white text-dark border'>")
                           .append(item.trim())
                           .append("</span>");
            }
            detailsHtml.append("</div>");
        }

        detailsHtml.append("</div></div>");

        return "<div class='facility-card'>" +
               "  <h2 class='dark-header'>" + name + "</h2>" +
               "  <div class='sub-grid'>" +
               "    <div class='sub-card' style='grid-column: 1 / -1; text-align: left;'>" + 
               "       <p>" + description + "</p>" +
               "       <img src='" + imageUrl + "' alt='" + name + "' class='card-image full-width' style='height: 300px; object-fit: cover;' />" +
               "       " + detailsHtml.toString() + 
               "    </div>" +
               "  </div>" +
               "</div>";
    }

    private String getAddButton() {
        return "<div class='d-flex justify-content-end mb-4'>" +
               "  <button type=\"button\" class=\"btn btn-primary\" data-bs-toggle=\"modal\" data-bs-target=\"#addFacilityModal\">" +
               "    + Add New Facility" +
               "  </button>" +
               "</div>";
    }

    private String getAddForm() {
        return "<div class=\"modal fade\" id=\"addFacilityModal\" tabindex=\"-1\" aria-hidden=\"true\">\n" +
                "  <div class=\"modal-dialog modal-dialog-centered\">\n" +
                "    <div class=\"modal-content\">\n" +
                "      <div class=\"modal-header bg-light\">\n" +
                "        <h5 class=\"modal-title dark-header\">Add a New Facility</h5>\n" +
                "        <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\n" +
                "      </div>\n" +
                "      <div class=\"modal-body\">\n" +
                "        <form action=\"facilities\" method=\"POST\">\n" +
                "          <div class=\"mb-3\">\n" +
                "            <label class=\"form-label text-muted small\">Facility Name</label>\n" +
                "            <input type=\"text\" name=\"name\" class=\"form-control\" required placeholder=\"e.g. Indoor Pool\">\n" +
                "          </div>\n" +
                "          <div class=\"mb-3\">\n" +
                "            <label class=\"form-label text-muted small\">Timings</label>\n" +
                "            <input type=\"text\" name=\"timings\" class=\"form-control\" placeholder=\"e.g. 8:00 AM - 10:00 PM\">\n" +
                "          </div>\n" +
                "          <div class=\"mb-3\">\n" +
                "            <label class=\"form-label text-muted small\">Amenities (comma separated)</label>\n" +
                "            <input type=\"text\" name=\"amenities\" class=\"form-control\" placeholder=\"e.g. Towels, Lockers, Showers\">\n" +
                "          </div>\n" +
                "          <div class=\"mb-3\">\n" +
                "            <label class=\"form-label text-muted small\">Description</label>\n" +
                "            <textarea name=\"description\" class=\"form-control\" rows=\"3\" required placeholder=\"Short description...\"></textarea>\n" +
                "          </div>\n" +
                "          <div class=\"mb-3\">\n" +
                "            <label class=\"form-label text-muted small\">Image URL</label>\n" +
                "            <input type=\"url\" name=\"image_url\" class=\"form-control\" required placeholder=\"https://example.com/image.jpg\">\n" +
                "          </div>\n" +
                "          <div class=\"d-grid gap-2 mt-4\">\n" +
                "            <button type=\"submit\" class=\"btn btn-primary\">Add Facility</button>\n" +
                "          </div>\n" +
                "        </form>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>";
    }
}