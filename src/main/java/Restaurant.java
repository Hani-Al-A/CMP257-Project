
import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private int id;
    private String name;
    private String description;
    private String cuisineType;
    private String[] tags;
    private String imageUrl;
    private List<MenuItem> menu; // Holds the list of menu items for this restaurant

    public Restaurant(int id, String name, String description, String cuisineType, String[] tags, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cuisineType = cuisineType;
        this.imageUrl = imageUrl;
        this.menu = new ArrayList<>();
    }

    public void addMenuItem(MenuItem item) {
        this.menu.add(item);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCuisineType() { return cuisineType; }
    public String[] getTags() { return tags; }
    public String getImageUrl() { return imageUrl; }
    public List<MenuItem> getMenu() { return menu; }
}