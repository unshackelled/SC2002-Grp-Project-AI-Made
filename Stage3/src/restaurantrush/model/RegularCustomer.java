package restaurantrush.model;

/** An ordinary guest: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }
}
