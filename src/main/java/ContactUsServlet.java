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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

@WebServlet("/contactus")   
public class ContactUsServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
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
        
        String html = loadTemplate("contactus.html"); 

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(html);
    }

    
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        
        String fullName = request.getParameter("fullName");
        String email    = request.getParameter("email");
        String topic    = request.getParameter("topic");
        String message  = request.getParameter("message");
        String consent  = request.getParameter("consent"); // null if unchecked

        boolean emailSent = false;
        String emailError = null;
        try {
            emailSent = sendConfirmationEmail(fullName, email, topic, message);
        } catch (Exception ex) {
            emailError = ex.getMessage();
            ex.printStackTrace();
        }

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
        

        if (emailSent) {
            out.println("    <p class=\"mt-3 text-success\">A confirmation email was sent to "
                    + escape(email) + ".</p>");
        } else {
            out.println("    <p class=\"mt-3 text-muted\">We stored your message, "
                    + "but could not send a confirmation email.");
            if (emailError != null) {
                out.println(" (Reason: " + escape(emailError) + ")");
            }
            out.println("</p>");
        }
        

        out.println("    <a href=\"contactus\" class=\"btn btn-primary mt-3\">Back to Contact Page</a>");
        out.println("  </div>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
        
    }

    
    private String loadTemplate(String path) throws IOException {
        
        try (InputStream is = getServletContext().getResourceAsStream(path)) {
            if (is == null) {
               
                return "<h1>Error: template not found: " + path + "</h1>";
            }
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    
    private boolean sendConfirmationEmail(String fullName,
                                          String toEmail,
                                          String topic,
                                          String msgText) throws Exception {

        final String fromEmail = System.getenv("SMTP_EMAIL");
        final String password  = System.getenv("SMTP_PASSWORD");

        if (fromEmail == null || password == null) {
            throw new Exception("SMTP_EMAIL / SMTP_PASSWORD not set.");
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
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
        message.setSubject("We received your message – Beach Resort");

        String body = "Dear " + fullName + ",\n\n"
                + "Thank you for contacting Beach Resort.\n"
                + "Topic: " + topic + "\n\n"
                + "Your message:\n"
                + msgText + "\n\n"
                + "We will get back to you as soon as possible.\n\n"
                + "Best regards,\n"
                + "Beach Resort Team";

        message.setText(body);

        Transport.send(message);
        System.out.println("Contact email sent successfully to " + toEmail);

        return true;
    }

    
    private String escape(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}