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
import java.util.Scanner;

/**
 * Servlet implementation class IndexServlet
 */
@WebServlet(urlPatterns = {"/index.html", "/index"})

public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public IndexServlet() {
        super();
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
	        
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login?action=logout'>Logout</a>"); //show a logout if logged in
	    } else {
	        htmlTemplate = htmlTemplate.replace("{{login_link}}", "<a class='nav-link mx-3' href='login'>Login</a>"); // show login if not logged in
	    }
	    
	    
		
		if(user_id == -1) {
			welcomeMessage = "Plan Your Stay With Us";
		}
		else{
			welcomeMessage = "Welcome, " + name;
		}
		
		String finalHtml = htmlTemplate
				.replace("{{main_header}}", welcomeMessage);
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.print(finalHtml);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private String loadHtmlTemplate() throws IOException {
		// Obtain an InputStream for the HTML template file located in the web application co
		try(InputStream is = getServletContext().getResourceAsStream("index.html")){
			//Add logic to read input stream you could also use byte array ...
			// Create a Scanner to read the input stream,
			// Use "\\A" as the delimiter to read the entire input as a single token
			Scanner scanner = new Scanner(is). useDelimiter("\\A");
			// Check if there is any content to read
			// If there is, return the content as a String; otherwise, return an empty string
			return scanner.hasNext() ? scanner.next() : ""; // Read file contents
		}
		
	}

}
