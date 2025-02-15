import java.io.Serializable;

public class Item implements Serializable {
    private String id;
    private String title;
    private double price;
    private int number;

    public Item(String movieId, String title, double price, int number) {
        this.id = movieId;
        this.title = title;
        this.price = price;
        this.number = number;
    }

    public String getMovieId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public double getPrice() {
        return this.price;
    }

    public int getQuantity() {
        return this.number;
    }

    public void setQuantity(int quantity) {
        this.number = quantity;
    }
}