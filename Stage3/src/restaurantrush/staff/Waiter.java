package restaurantrush.staff;

import java.util.Optional;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Customer;
import restaurantrush.model.CustomerStatus;
import restaurantrush.model.HappyHour;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;
import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.Restaurant;

/** Seats customers, takes their orders and serves READY dishes, one task per turn. */
public class Waiter extends Staff {

    public Waiter(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Waiter";
    }

    public ActionResult seat(Restaurant restaurant, int customerId) {
        if (hasActed()) {
            return alreadyActed();
        }
        ActionResult result = restaurant.seatFromQueue(customerId);
        if (result.isSuccess()) {
            completeTask();
        }
        return result;
    }

    /**
     * The customer chooses their own food; the manager only decides whose order to take.
     * The bill is fixed now: the customer's own discount first, then Happy Hour if it is active.
     */
    public ActionResult takeOrder(Restaurant restaurant, int customerId, Menu menu, HappyHour happyHour, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        Optional<Customer> found = restaurant.findCustomer(customerId);
        if (!found.isPresent()) {
            return ActionResult.failure("There is no customer C" + customerId + ".");
        }
        Customer customer = found.get();
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return ActionResult.failure(customer.getLabel() + " has no order to take ("
                    + customer.getStatus().getLabel() + "). Only seated customers who have not ordered can order.");
        }
        MenuItem item = customer.chooseItem(menu);
        int bill = happyHour.applyTo(customer.calculateBill(item));
        Order order = restaurant.placeOrder(customer, item, bill, turn);
        completeTask();
        return ActionResult.success(customer.getLabel() + " orders " + item.getName() + " - bill "
                + Money.format(bill) + (happyHour.isActive() ? " (Happy Hour price)" : "")
                + ". Order #" + order.getId() + " sent to the kitchen.");
    }

    public ActionResult serve(Restaurant restaurant, int orderId, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        Optional<Order> found = restaurant.findOrder(orderId);
        if (!found.isPresent()) {
            return ActionResult.failure("There is no order #" + orderId + ".");
        }
        Order order = found.get();
        if (order.getStatus() != OrderStatus.READY) {
            return ActionResult.failure("Order #" + orderId + " is " + order.getStatus()
                    + "; only READY orders can be served.");
        }
        if (!order.isServableOn(turn)) {
            return ActionResult.failure("Order #" + orderId
                    + " only became READY this turn; it can be served next turn at the earliest.");
        }
        order.serve();
        order.getCustomer().receiveFood(turn);
        restaurant.getScoreboard().recordServed();
        completeTask();
        return ActionResult.success("Order #" + orderId + " (" + order.getItem().getName() + ") SERVED to "
                + order.getCustomer().getLabel() + ".");
    }

    @Override
    public boolean hasPossibleTask(Restaurant restaurant, int turn) {
        boolean canSeat = restaurant.hasFreeTable() && !restaurant.getWaitingQueue().isEmpty();
        return canSeat
                || !restaurant.getCustomersWithStatus(CustomerStatus.SEATED).isEmpty()
                || !restaurant.getServableOrders(turn).isEmpty();
    }

    @Override
    public String describeAvailableTasks(Restaurant restaurant, int turn) {
        long freeTables = restaurant.getTables().stream().filter(t -> t.isFree()).count();
        return String.format("seat (%d queued, %d free table%s) | take order (%d seated) | serve (%d READY)",
                restaurant.getWaitingQueue().size(), freeTables, freeTables == 1 ? "" : "s",
                restaurant.getCustomersWithStatus(CustomerStatus.SEATED).size(),
                restaurant.getServableOrders(turn).size());
    }
}
