package restaurantrush.model;

/**
 * Money helpers. Every amount in the game is stored as integer cents so that
 * no rounding error can creep into revenue; dollars are only used for display.
 */
public final class Money {

    private Money() {
    }

    /** Formats an amount of cents as dollars with two decimals, e.g. 1830 -> "$18.30". */
    public static String format(long cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    /** Returns percent% of an amount, rounded to the nearest cent, e.g. percentOf(1500, 90) = 1350. */
    public static int percentOf(int cents, int percent) {
        return (int) ((cents * (long) percent + 50) / 100);
    }
}
