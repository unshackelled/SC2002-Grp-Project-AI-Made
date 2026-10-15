package restaurantrush.model;

/** Running totals used for the end-of-turn display and the final result. */
public class Scoreboard {

    private long revenueCents;
    private int paidCount;
    private int satisfactionTotal;
    private int servedCount;
    private int unhappyDepartures;

    public void recordServed() {
        servedCount++;
    }

    /** Only paid bills ever reach revenue. */
    public void recordPayment(int billCents, int satisfactionAtPayment) {
        revenueCents += billCents;
        paidCount++;
        satisfactionTotal += satisfactionAtPayment;
    }

    public void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    public boolean hasPaidCustomers() {
        return paidCount > 0;
    }

    /** Average satisfaction recorded at payment; only meaningful if someone has paid. */
    public double getAverageSatisfaction() {
        return paidCount == 0 ? 0.0 : (double) satisfactionTotal / paidCount;
    }

    public long getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getSatisfactionTotal() {
        return satisfactionTotal;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }
}
