import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.Scanner;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = { "/roomBooking", "/roomBooking.html" })
public class RoomBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RoomBookingServlet() {
        super();
    }

    // SHOW BOOKING FORM
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String htmlTemplate = loadTemplate("RoomBooking.html");

        if (session != null && session.getAttribute("user") != null) {
            htmlTemplate = htmlTemplate.replace("{{login_link}}",
                    "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>");
        } else {
            htmlTemplate = htmlTemplate.replace("{{login_link}}",
                    "<a class='nav-link mx-3' href='login'>Login</a>");
        }

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect("rooms");
            return;
        }

        String roomName = "Selected Room";
        double pricePerNight = 0.0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT name, price_per_night FROM rooms WHERE room_id = ?")) {

            ps.setInt(1, Integer.parseInt(idParam));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                roomName = rs.getString("name");
                pricePerNight = rs.getDouble("price_per_night");
            } else {
                response.getWriter().println("Room not found.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error loading room details: " + e.getMessage());
            return;
        }

        String fullHtml = htmlTemplate
                .replace("{{room_name}}", roomName)
                .replace("{{room_id}}", idParam)
                .replace("{{price_per_night}}", String.format("%.0f", pricePerNight));

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(fullHtml);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Integer userId = null;
        String username = "";
        if (session != null && session.getAttribute("userId") != null) {
            userId = (Integer) session.getAttribute("userId");
            username = (String) session.getAttribute("user");
        }

        String roomIdStr   = request.getParameter("room_id");
        String guestName   = request.getParameter("guest_name");
        String guestEmail  = request.getParameter("guest_email");
        String guestPhone  = request.getParameter("guest_phone");
        String checkInStr  = request.getParameter("check_in");
        String checkOutStr = request.getParameter("check_out");
        String guests      = request.getParameter("guests");

        if (roomIdStr == null || checkInStr == null || checkOutStr == null) {
            showResultPage(response, false, "Missing booking information.");
            return;
        }

        try {
            int roomId = Integer.parseInt(roomIdStr);
            LocalDate checkIn  = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) {
                showResultPage(response, false, "Check-out date must be after check-in date.");
                return;
            }

            double pricePerNight = 0.0;
            String roomName = "your room";

            // Get price and room name
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT name, price_per_night FROM rooms WHERE room_id = ?")) {

                ps.setInt(1, roomId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    roomName = rs.getString("name");
                    pricePerNight = rs.getDouble("price_per_night");
                } else {
                    showResultPage(response, false, "Room not found.");
                    return;
                }
            }

            double totalPrice = pricePerNight * nights;

            // Insert booking
            String sql = "INSERT INTO bookings "
                    + "(user_id, room_id, check_in_date, check_out_date, total_price, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                if (userId == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, userId);
                }

                ps.setInt(2, roomId);
                ps.setDate(3, java.sql.Date.valueOf(checkIn));
                ps.setDate(4, java.sql.Date.valueOf(checkOut));
                ps.setDouble(5, totalPrice);
                ps.setString(6, "Confirmed");

                int rows = ps.executeUpdate();
                String message;
                boolean success;

                if (rows > 0) {
                    success = true;
                    message = "Your stay is booked from " + checkInStr + " to "
                            + checkOutStr + ". Total price: AED "
                            + String.format("%.2f", totalPrice) + ".";

                    try {
                        sendConfirmationEmail(guestEmail, guestName, roomName,
                                checkInStr, checkOutStr, guests, totalPrice);
                    } catch (Exception e) {
                        System.out.println("EMAIL ERROR: " + e.getMessage());
                        e.printStackTrace();
                        message = message
                                + " However, we couldn't send the confirmation email.";
                    }
                } else {
                    success = false;
                    message = "Failed to save booking. Please try again.";
                }

                showResultPage(response, success, message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showResultPage(response, false, "Error: " + e.getMessage());
        }
    }

    private String loadTemplate(String path) throws IOException {
        try (InputStream is = getServletContext().getResourceAsStream(path)) {
            if (is == null) {
                return "<h1>Error: Template not found: " + path + "</h1>";
            }
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private void showResultPage(HttpServletResponse response, boolean success, String msg)
            throws IOException {
        String html = loadTemplate("result_page.html");

        html = html.replace("{{page_title}}", "Booking Status")
                   .replace("{{header_title}}", "Booking Status")
                   .replace("{{button_link}}", "rooms")
                   .replace("{{button_text}}", "Back to Rooms");

        if (success) {
            html = html.replace("{{text_color}}", "text-success")
                       .replace("{{status_icon}}", "✓")
                       .replace("{{status_title}}", "Booking Confirmed!")
                       .replace("{{status_message}}", msg);
        } else {
            html = html.replace("{{text_color}}", "text-danger")
                       .replace("{{status_icon}}", "⚠")
                       .replace("{{status_title}}", "Booking Failed")
                       .replace("{{status_message}}", msg);
        }

        response.setContentType("text/html");
        response.getWriter().print(html);
    }

    private void sendConfirmationEmail(String toEmail, String name, String roomName, String checkIn, String checkOut, String guests, double totalPrice) throws Exception {

        final String fromEmail = System.getenv("SMTP_EMAIL");
        final String password  = System.getenv("SMTP_PASSWORD");

        if (fromEmail == null || password == null) {
            throw new Exception("SMTP environment variables not set.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO,
                              InternetAddress.parse(toEmail));
        message.setSubject("Room Booking Confirmed - Coastal Haven");
        message.setText("Dear " + name + ",\n\n"
                + "Your room booking is confirmed!\n"
                + "Room: " + roomName + "\n"
                + "Check-in: " + checkIn + "\n"
                + "Check-out: " + checkOut + "\n"
                + "Guests: " + guests + "\n"
                + "Total price: AED " + String.format("%.2f", totalPrice) + "\n\n"
                + "We look forward to welcoming you.\n\n");
                
        Transport.send(message);
        System.out.println("Room booking email sent to " + toEmail);
    }
}