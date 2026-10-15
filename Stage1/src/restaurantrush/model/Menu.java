package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The ordered list of items the restaurant offers. */
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    public void add(MenuItem item) {
        items.add(item);
    }

    public MenuItem getItem(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
