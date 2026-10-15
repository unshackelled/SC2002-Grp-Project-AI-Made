package restaurantrush.model;

/**
 * Two existing menu items sold together. Its preparation units are the sum of
 * the two parts, and its price is 90% of their combined price, rounded to the
 * nearest cent. For example, Salad + Pasta = ($9 + $18) x 0.9 = $24.30 and 3 units.
 * Since it is a MenuItem, Orders, the Chef and customers treat it like any other dish.
 */
public class ComboMeal extends MenuItem {

    public static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo");
        this.first = first;
        this.second = second;
    }

    @Override
    public int getPriceCents() {
        return Money.percentOf(first.getPriceCents() + second.getPriceCents(), PRICE_PERCENT);
    }

    @Override
    public int getPrepUnits() {
        return first.getPrepUnits() + second.getPrepUnits();
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
