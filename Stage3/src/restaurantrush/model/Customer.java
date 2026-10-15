package restaurantrush.model;

/**
 * A guest of the restaurant. It holds the guest's satisfaction and keeps track
 * of where the guest is in the service flow. Subclasses decide how quickly
 * satisfaction drops, what the guest orders and how the bill is worked out.
 * The turn controller never checks which subclass it has.
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private final int arrivalTurn;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;
    private int recordedSatisfaction = -1;

    protected Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    // ----- behaviour that subclasses specialise -----

    /** Short type name shown to the player, e.g. "Regular". */
    public abstract String getType();

    /** Satisfaction lost at the end of each turn spent waiting for a table or food. */
    public abstract int getSatisfactionLoss();

    /**
     * Default ordering rule: customers rotate through the menu in listed order
     * by arrival number (C1 takes item 1, C2 item 2, and so on, wrapping round).
     */
    public MenuItem chooseItem(Menu menu) {
        return menu.getItem((id - 1) % menu.size());
    }

    /** Bill in cents for the chosen item. Regular customers pay the menu price. */
    public int calculateBill(MenuItem item) {
        return item.getPriceCents();
    }

    // ----- state transitions -----

    public void seatAt(Table newTable) {
        requireStatus(CustomerStatus.WAITING);
        newTable.assign(this);
        table = newTable;
        status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order newOrder) {
        requireStatus(CustomerStatus.SEATED);
        order = newOrder;
        status = CustomerStatus.ORDERED;
    }

    public void receiveFood(int turn) {
        requireStatus(CustomerStatus.ORDERED);
        servedTurn = turn;
        status = CustomerStatus.EATING;
    }

    /**
     * A customer served on an earlier turn finishes one full turn of eating.
     *
     * @return true if the customer just became READY_TO_PAY
     */
    public boolean finishEating(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    /** A customer who became READY_TO_PAY this turn pays next turn at the earliest. */
    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn;
    }

    public void pay() {
        requireStatus(CustomerStatus.READY_TO_PAY);
        recordedSatisfaction = satisfaction;
        status = CustomerStatus.PAID;
        vacateTable();
    }

    /**
     * Applies the end-of-turn waiting penalty (never below 0).
     * Customers who have been served lose nothing.
     */
    public void loseSatisfactionForWaiting() {
        if (status.isWaitingForService()) {
            satisfaction = Math.max(0, satisfaction - getSatisfactionLoss());
        }
    }

    /** Restores satisfaction, never above the maximum (used by Happy Hour). */
    public void recover(int amount) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }

    public void leave() {
        if (!status.isWaitingForService()) {
            throw new IllegalStateException(getLabel() + " cannot leave while " + status);
        }
        status = CustomerStatus.LEFT;
        vacateTable();
    }

    private void vacateTable() {
        if (table != null) {
            table.vacate();
            table = null;
        }
    }

    private void requireStatus(CustomerStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(getLabel() + " is " + status + ", expected " + expected);
        }
    }

    // ----- queries -----

    public boolean isWaitingForService() {
        return status.isWaitingForService();
    }

    public String getLabel() {
        return "C" + id + " [" + getType() + "]";
    }

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public int getRecordedSatisfaction() {
        return recordedSatisfaction;
    }
}
