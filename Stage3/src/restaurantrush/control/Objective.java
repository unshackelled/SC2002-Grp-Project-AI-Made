package restaurantrush.control;

import restaurantrush.model.Scoreboard;

/** The three targets that must all be met at the end of the game for a Victory. */
public class Objective {

    private final int minPaidCustomers;
    private final long minRevenueCents;
    private final int minAverageSatisfaction;

    public Objective(int minPaidCustomers, long minRevenueCents, int minAverageSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public boolean isPaidCustomersMet(Scoreboard score) {
        return score.getPaidCount() >= minPaidCustomers;
    }

    public boolean isRevenueMet(Scoreboard score) {
        return score.getRevenueCents() >= minRevenueCents;
    }

    /** Compared with whole numbers (total >= target x count) to avoid floating-point error. */
    public boolean isSatisfactionMet(Scoreboard score) {
        return score.hasPaidCustomers()
                && score.getSatisfactionTotal() >= (long) minAverageSatisfaction * score.getPaidCount();
    }

    public boolean isMet(Scoreboard score) {
        return isPaidCustomersMet(score) && isRevenueMet(score) && isSatisfactionMet(score);
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public long getMinRevenueCents() {
        return minRevenueCents;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
