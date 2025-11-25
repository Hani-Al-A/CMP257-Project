@WebServlet("/addMenuItem")
public class AddMenuItemServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) { resp.sendRedirect("login.html"); return; }

        int restaurantId = Integer.parseInt(req.getParameter("restaurant_id"));
        String itemName = req.getParameter("item_name");
        String description = req.getParameter("description");
        double price = Double.parseDouble(req.getParameter("price"));
        String category = req.getParameter("category");

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO menu_items (restaurant_id, item_name, description, price, category) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, restaurantId);
            ps.setString(2, itemName);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setString(5, category);
            ps.executeUpdate();
            resp.sendRedirect("admin-dashboard.html");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().println("Error adding menu item: " + e.getMessage());
        }
    }
}
