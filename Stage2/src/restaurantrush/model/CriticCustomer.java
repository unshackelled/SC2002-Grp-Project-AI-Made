package restaurantrush.model;

/**
 * A food critic: impatient (loses 12 per waiting turn) and pays full price.
 * Special behaviour: the critic ignores the normal rotation and always orders
 * the most expensive item on the menu (the first one listed if prices tie),
 * which is usually also the slowest dish to cook.
 */
public class CriticCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 12;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem best = menu.getItem(0);
        for (MenuItem item : menu.getItems()) {
            if (item.getPriceCents() > best.getPriceCents()) {
                best = item;
            }
        }
        return best;
    }
}
