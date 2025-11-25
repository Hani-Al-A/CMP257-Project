// ContactUsServlet.java
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

@WebServlet("/ContactUsServlet")
public class ContactUsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email    = request.getParameter("email");
        String topic    = request.getParameter("topic");
        String message  = request.getParameter("message");
        String consent  = request.getParameter("consent");

        // ---- NEW: SEND EMAIL ----
        boolean emailSent = false;
        String emailError = "";

        try {
            sendEmail(fullName, email, topic, message);
            emailSent = true;
        } catch (Exception e) {
            emailSent = false;
            emailError = e.getMessage();
            e.printStackTrace();
        }
        // --------------------------

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <title>Contact Confirmation</title>");
        out.println("  <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css\" rel=\"stylesheet\">");
        out.println("</head>");
        out.println("<body class=\"bg-light\">");

        out.println("<div class=\"container mt-5\">");
        out.println("  <div class=\"alert alert-success\">");
        out.println("    <h4 class=\"alert-heading\">Thank you, " + escape(fullName) + "!</h4>");
        out.println("    <p>Your \"" + escape(topic) + "\" message has been received.</p>");
        out.println("    <hr>");
        out.println("    <p><strong>Email:</strong> " + escape(email) + "</p>");
        out.println("    <p><strong>Message:</strong><br>" +
                escape(message).replace("\n", "<br>") + "</p>");

        if (consent != null) {
            out.println("    <p>You agreed to be contacted about your inquiry.</p>");
        } else {
            out.println("    <p>You did not agree to be contacted.</p>");
        }

        // ---- NEW FEEDBACK ABOUT EMAIL ----
        if (emailSent) {
            out.println("<p class='text-success mt-3'>A confirmation email has been sent.</p>");
        } else {
            out.println("<p class='text-danger mt-3'>Email could NOT be sent: " + escape(emailError) + "</p>");
        }
        // ------------------------------------

        out.println("    <a href=\"contactus.html\" class=\"btn btn-primary mt-3\">Back to Contact Page</a>");
        out.println("  </div>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    // SAME ESCAPE FUNCTION YOU WROTE
    private String escape(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ---- NEW EMAIL SENDING METHOD (same style as ReservationServlet) ----
    private void sendEmail(String fullName, String fromEmail, String topic, String userMessage) throws Exception {

        final String resortEmail = System.getenv("SMTP_EMAIL");     // Your Gmail
        final String resortPass  = System.getenv("SMTP_PASSWORD");  // Gmail App Password

        if (resortEmail == null || resortPass == null) {
            throw new Exception("SMTP environment variables not set.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(resortEmail, resortPass);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(resortEmail));
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(resortEmail)); // Sends to resort inbox
        msg.setSubject("New Contact Inquiry: " + topic);

        String body = "New inquiry submitted:\n\n"
                + "Name: " + fullName + "\n"
                + "Email: " + fromEmail + "\n"
                + "Topic: " + topic + "\n\n"
                + "Message:\n" + userMessage + "\n";

        msg.setText(body);

        Transport.send(msg);
    }
}
