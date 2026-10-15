package restaurantrush.staff;

import java.util.Comparator;
import java.util.Optional;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Customer;
import restaurantrush.model.Money;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;

/** Automatically takes at most one payment per turn, oldest READY_TO_PAY customer first. */
public class Cashier extends Staff {

    public Cashier(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Cashier";
    }

    public ActionResult collectPayment(Restaurant restaurant, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        Optional<Customer> next = nextPayer(restaurant, turn);
        if (!next.isPresent()) {
            return ActionResult.failure("No customer is ready to pay.");
        }
        Customer customer = next.get();
        Order order = customer.getOrder();
        int tableNumber = customer.getTable().getNumber();
        order.markPaid();
        customer.pay();
        restaurant.getScoreboard().recordPayment(order.getBillCents(), customer.getRecordedSatisfaction());
        completeTask();
        return ActionResult.success("Cashier collected " + Money.format(order.getBillCents()) + " from "
                + customer.getLabel() + ": PAID with satisfaction " + customer.getRecordedSatisfaction()
                + " recorded. Table " + tableNumber + " is free.");
    }

    private Optional<Customer> nextPayer(Restaurant restaurant, int turn) {
        return restaurant.getCustomers().stream()
                .filter(c -> c.canPayOn(turn))
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId));
    }

    @Override
    public boolean hasPossibleTask(Restaurant restaurant, int turn) {
        return nextPayer(restaurant, turn).isPresent();
    }

    @Override
    public String describeAvailableTasks(Restaurant restaurant, int turn) {
        long count = restaurant.getCustomers().stream().filter(c -> c.canPayOn(turn)).count();
        return "automatic - " + count + " customer" + (count == 1 ? "" : "s") + " can pay this turn";
    }
}
