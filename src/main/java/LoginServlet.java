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

@WebServlet(urlPatterns = {"/login", "/login.html"})
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LoginServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 1. Handle Logout logic (e.g., link href="login?action=logout")
		HttpSession session = request.getSession(false); // get session, don't create new one
		
	    String name = "";
	    int user_id = -1;
	    String email = "";
	    boolean isAdmin = false;
	    
	    String htmlTemplate = loadTemplate("login.html");
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
		
		String action = request.getParameter("action");
		if("logout".equals(action)) {
			session = request.getSession(false);
			if(session != null) {
				session.invalidate(); // Destroys the session completely
			}
			response.sendRedirect("index.html");
			return;
		}
		
		if(session != null && session.getAttribute("user") != null) {
			response.sendRedirect("index.html");
			return;
		}

		htmlTemplate = htmlTemplate.replace("{{error_message}}", "");
		
		response.setContentType("text/html");
		response.getWriter().print(htmlTemplate);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		
		String errorMsg = "";
		boolean isSuccess = false;
		
		try (Connection conn = DBConnection.getConnection()) {
			
			if("login".equals(action)) {
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				
				String sql = "SELECT user_id, username, email, is_admin FROM users WHERE username = ? AND password = ?";
				try(PreparedStatement ps = conn.prepareStatement(sql)) {
					ps.setString(1, username);
					ps.setString(2, password);
					ResultSet rs = ps.executeQuery();
					
					if(rs.next()) {
						HttpSession session = request.getSession();
						session.setAttribute("user", username);
						session.setAttribute("userId", rs.getInt("user_id"));
						session.setAttribute("email", rs.getString("email"));
						session.setAttribute("isAdmin", rs.getBoolean("is_admin"));
						
						response.sendRedirect("index.html"); 
						return; 
					} else {
						errorMsg = "Invalid username or password.";
					}
				}
			} 
			else if ("register".equals(action)) {
				String username = request.getParameter("username");
				String email = request.getParameter("email");
				String password = request.getParameter("password");
				
				String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
				try(PreparedStatement ps = conn.prepareStatement(sql)) {
					ps.setString(1, username);
					ps.setString(2, email);
					ps.setString(3, password);
					ps.executeUpdate();
					
					isSuccess = true;
					errorMsg = "<span class='text-success fw-bold'>Account created! Please login.</span>";
				} catch (Exception e) {
					errorMsg = "Username or Email already exists.";
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			errorMsg = "Database Connection Error.";
		}
		

		String html = loadTemplate("login.html");
		html = html.replace("{{error_message}}", errorMsg);
				
		response.setContentType("text/html");
		response.getWriter().print(html);
	}
	
	private String loadTemplate(String path) throws IOException {
		try(InputStream is = getServletContext().getResourceAsStream(path)){
			if(is == null) return "<h1>Error: Template not found at " + path + "</h1>";
			Scanner scanner = new Scanner(is).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : ""; 
		}
	}
}