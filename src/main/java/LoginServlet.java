import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT is_admin FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password); // ideally hashed in production

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean isAdmin = rs.getBoolean("is_admin");
                HttpSession session = req.getSession();
                session.setAttribute("username", username);
                session.setAttribute("isAdmin", isAdmin);

                if (isAdmin) {
                    resp.sendRedirect("admin-dashboard.html");
                } else {
                    resp.sendRedirect("index.html");
                }
            } else {
                resp.sendRedirect("login.html?error=1");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Database error: " + e.getMessage());
        }
    }
}
