package restaurantrush.model;

/** A single dish with a fixed price and preparation time (Salad, Burger, Pasta). */
public class Dish extends MenuItem {

    private final int priceCents;
    private final int prepUnits;

    public Dish(String name, int priceCents, int prepUnits) {
        super(name);
        if (priceCents < 0 || prepUnits < 1) {
            throw new IllegalArgumentException("Invalid dish: " + name);
        }
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    @Override
    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public int getPrepUnits() {
        return prepUnits;
    }
}
