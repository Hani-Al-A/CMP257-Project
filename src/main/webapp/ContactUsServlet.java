// ContactUsServlet.java
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

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

        out.println("    <a href=\"contactus.html\" class=\"btn btn-primary mt-3\">Back to Contact Page</a>");
        out.println("  </div>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
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
