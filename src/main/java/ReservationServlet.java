
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import jakarta.mail.*;
import jakarta.mail.internet.*;


@WebServlet("/reservation")
public class ReservationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ReservationServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idParam = request.getParameter("id");// comes in the query as ?id=X
		if(idParam == null) { response.sendRedirect("dining"); return; }
		
		String restName = "the restaurant";
		try (Connection conn = DBConnection.getConnection();
		     Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT name FROM restaurants WHERE restaurant_id=" + idParam);
			if(rs.next()) restName = rs.getString("name");
		} catch (Exception e) { e.printStackTrace(); }

		String html = loadTemplate("reservation.html");
		html = html.replace("{{restaurant_name}}", restName)
		           .replace("{{restaurant_id}}", idParam);
		
		response.setContentType("text/html");
		response.getWriter().print(html);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String restaurantId = request.getParameter("restaurant_id");
		String name = request.getParameter("guest_name");
		String email = request.getParameter("guest_email");
		String phone = request.getParameter("guest_phone");
		String date = request.getParameter("date");
		String time = request.getParameter("time");
		String guests = request.getParameter("guests");
		
		boolean success = false;
		String message = "";
		
		String sql = "INSERT INTO restaurant_reservations (restaurant_id, guest_name, guest_email, guest_phone, reservation_date, reservation_time, party_size) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1, Integer.parseInt(restaurantId));
			ps.setString(2, name);
			ps.setString(3, email);
			ps.setString(4, phone);
			ps.setString(5, date);
			ps.setString(6, time + ":00"); 
			ps.setInt(7, Integer.parseInt(guests));
			
			int rows = ps.executeUpdate();
			if(rows > 0) {
				success = true;
				message = "Your table has been successfully booked. A confirmation has been sent to " + email;
				
				try {
					sendConfirmationEmail(email, name, date, time, guests);
				} catch (Exception e) {
					System.out.println("EMAIL ERROR: " + e.getMessage());
					e.printStackTrace();
					message = "Your table is booked, but we couldn't send the confirmation email.";
				}
				
			} else {
				message = "Failed to save reservation. Please try again.";
			}
			
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
			message = "Database Error: " + e.getMessage();
		}
		
		showResultPage(response, success, message);
	}
	
	private void showResultPage(HttpServletResponse response, boolean success, String msg) throws IOException {
		String html = loadTemplate("result_page.html");
		
		html = html.replace("{{page_title}}", "Reservation Status")
		           .replace("{{header_title}}", "Reservation Status")
		           .replace("{{button_link}}", "dining")
		           .replace("{{button_text}}", "Return to Dining");
		
		if(success) {
			html = html.replace("{{text_color}}", "text-success")
			           .replace("{{status_icon}}", "✓")
			           .replace("{{status_title}}", "Reservation Confirmed!")
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

	private void sendConfirmationEmail(String toEmail, String name, String date, String time, String guests) throws Exception {
		final String fromEmail = System.getenv("SMTP_EMAIL");
		final String password = System.getenv("SMTP_PASSWORD");
		
		if(fromEmail == null || password == null) {
			throw new Exception("SMTP Environment variables not set.");
		}

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(fromEmail));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
		message.setSubject("Reservation Confirmed - Coastal Haven");
		message.setText("Dear " + name + ",\n\n"
				+ "Your reservation is confirmed!\n"
				+ "Date: " + date + "\n"
				+ "Time: " + time + "\n"
				+ "Guests: " + guests + "\n\n"
				+ "We look forward to serving you.");

		Transport.send(message);
		System.out.println("Email sent successfully to " + toEmail);
	}
	
	private String loadTemplate(String path) throws IOException {
		try(InputStream is = getServletContext().getResourceAsStream(path)){
			if(is == null) return "<h1>Error: Template not found: " + path + "</h1>";
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : ""; 
		}
	}
}