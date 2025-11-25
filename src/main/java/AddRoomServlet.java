@WebServlet("/addRoom")
public class AddRoomServlet extends HttpServlet {

    private static final String URL = "jdbc:mysql://localhost:3306/coastal_haven";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String roomNum = req.getParameter("room_number");
        String type = req.getParameter("type");
        String price = req.getParameter("price");
        String image = req.getParameter("image");

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO rooms (room_number, type, price, image) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, roomNum);
            ps.setString(2, type);
            ps.setString(3, price);
            ps.setString(4, image);

            ps.executeUpdate();
            resp.sendRedirect("admin-dashboard.html");

        } catch (Exception e) {
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
