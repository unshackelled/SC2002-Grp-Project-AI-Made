package restaurantrush.model;

/**
 * Pricing policy the manager can switch on once per game, at the start of a
 * turn. It lasts for that turn and the next. Orders taken while it is active
 * get 20% off, applied after any customer discount. Customers still waiting
 * for a table or food recover 15 satisfaction at the start of each active turn.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int BILL_PERCENT = 80;
    public static final int RECOVERY = 15;

    private boolean used;
    private int remainingTurns;

    public boolean isAvailable() {
        return !used;
    }

    public boolean isActive() {
        return remainingTurns > 0;
    }

    /** Active turns left, counting the current one. */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void activate() {
        if (used) {
            throw new IllegalStateException("Happy Hour can only be activated once per game");
        }
        used = true;
        remainingTurns = DURATION_TURNS;
    }

    /** Applies the Happy Hour discount to a bill if the policy is active right now. */
    public int applyTo(int billCents) {
        return isActive() ? Money.percentOf(billCents, BILL_PERCENT) : billCents;
    }

    public void endTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }
}
