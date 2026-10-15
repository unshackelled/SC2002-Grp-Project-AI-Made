package restaurantrush.model;

/** Where a customer is in the service flow. */
public enum CustomerStatus {
    WAITING("Waiting for a table", true),
    SEATED("Seated, waiting to order", true),
    ORDERED("Waiting for food", true),
    EATING("Eating", false),
    READY_TO_PAY("Ready to pay", false),
    PAID("Paid", false),
    LEFT("Left unhappy", false);

    private final String label;
    private final boolean waitingForService;

    CustomerStatus(String label, boolean waitingForService) {
        this.label = label;
        this.waitingForService = waitingForService;
    }

    public String getLabel() {
        return label;
    }

    /** True while the customer has not been served yet and therefore loses satisfaction. */
    public boolean isWaitingForService() {
        return waitingForService;
    }
}
