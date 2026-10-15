package restaurantrush.model;

/**
 * Something a customer can order. Callers only rely on the price and the
 * number of preparation units, so new kinds of item can be added without
 * changing the Waiter, Chef or Order.
 */
public abstract class MenuItem {

    private final String name;

    protected MenuItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Menu price in cents. */
    public abstract int getPriceCents();

    /** Number of Chef turns needed to prepare the item. */
    public abstract int getPrepUnits();

    @Override
    public String toString() {
        return String.format("%s (%s, %d unit%s)", name, Money.format(getPriceCents()),
                getPrepUnits(), getPrepUnits() == 1 ? "" : "s");
    }
}
