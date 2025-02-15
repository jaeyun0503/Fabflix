import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Cart implements Serializable {
    private Map<String, Item> items = new HashMap<>();

    public void addItem(String id, String title, double price) {
        if (items.containsKey(id)) {
            Item item = items.get(id);
            item.setQuantity(item.getQuantity() + 1);
        } else {
            items.put(id, new Item(id, title, price, 1));
        }
    }

    public void updateItem(String id, int quantity) {
        if (items.containsKey(id)) {
            if (quantity <= 0) {
                items.remove(id);
            } else {
                items.get(id).setQuantity(quantity);
            }
        }
    }

    public void removeItem(String id) {
        items.remove(id);
    }

    public Collection<Item> getItems() {
        return items.values();
    }

    public double getTotalPrice() {
        double total = 0;
        for (Item item : items.values()) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

}
