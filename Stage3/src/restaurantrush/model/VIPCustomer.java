package restaurantrush.model;

/** A valued guest: more patient (loses 5 per waiting turn) and gets 10% off the bill. */
public class VIPCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 5;
    public static final int BILL_PERCENT = 90;

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }

    @Override
    public int calculateBill(MenuItem item) {
        return Money.percentOf(item.getPriceCents(), BILL_PERCENT);
    }
}
