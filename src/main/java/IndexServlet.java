

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
		//Get the user logjn, if theyre logged in
		String htmlTemplate = loadHtmlTemplate();
		String name = "Valued Guest"; // if logged in
		String welcomeMessage = "";
		
		if(name.isBlank()) {
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
