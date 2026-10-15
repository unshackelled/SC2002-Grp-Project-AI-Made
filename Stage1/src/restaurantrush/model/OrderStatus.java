package restaurantrush.model;

/** Life cycle of an order from the moment the Waiter takes it. */
public enum OrderStatus {
    PLACED,
    IN_PREPARATION,
    READY,
    SERVED,
    PAID,
    CANCELLED;

    /** True while the Chef can still work on the order. */
    public boolean isInKitchen() {
        return this == PLACED || this == IN_PREPARATION;
    }
}
