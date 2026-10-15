package restaurantrush.model;

/**
 * One customer's order. The bill is fixed when the order is taken and never
 * changes afterwards. Order numbers match the customer's number (order #3
 * belongs to C3) so the player can easily tell which order is whose.
 */
public class Order {

    private final int id;
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private final int takenTurn;
    private int unitsDone;
    private int readyTurn = -1;
    private OrderStatus status = OrderStatus.PLACED;

    public Order(int id, Customer customer, MenuItem item, int billCents, int takenTurn) {
        this.id = id;
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.takenTurn = takenTurn;
    }

    /**
     * Chef work: completes one preparation unit.
     *
     * @return true if this unit was the last one and the order is now READY
     */
    public boolean prepareUnit(int turn) {
        requireInKitchen();
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
            return true;
        }
        status = OrderStatus.IN_PREPARATION;
        return false;
    }

    /** A dish can only be served if it was already READY before this turn. */
    public boolean isServableOn(int turn) {
        return status == OrderStatus.READY && readyTurn < turn;
    }

    public void serve() {
        requireStatus(OrderStatus.READY);
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        requireStatus(OrderStatus.SERVED);
        status = OrderStatus.PAID;
    }

    /** Cancels an unfinished order when its customer leaves. */
    public void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("Order #" + id + " is already " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireInKitchen() {
        if (!status.isInKitchen()) {
            throw new IllegalStateException("Order #" + id + " is not in the kitchen (" + status + ")");
        }
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order #" + id + " is " + status + ", expected " + expected);
        }
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getTakenTurn() {
        return takenTurn;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
