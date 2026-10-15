package restaurantrush.staff;

import java.util.Optional;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;

/** Advances one order by one preparation unit per turn. */
public class Chef extends Staff {

    public Chef(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Chef";
    }

    public ActionResult prepare(Restaurant restaurant, int orderId, int turn) {
        if (hasActed()) {
            return alreadyActed();
        }
        Optional<Order> found = restaurant.findOrder(orderId);
        if (!found.isPresent()) {
            return ActionResult.failure("There is no order #" + orderId + ".");
        }
        Order order = found.get();
        if (!order.getStatus().isInKitchen()) {
            return ActionResult.failure("Order #" + orderId + " is " + order.getStatus()
                    + "; the Chef can only work on orders still being prepared.");
        }
        boolean ready = order.prepareUnit(turn);
        completeTask();
        String progress = order.getUnitsDone() + "/" + order.getItem().getPrepUnits();
        if (ready) {
            return ActionResult.success("Chef prepared unit " + progress + " of order #" + orderId + " ("
                    + order.getItem().getName() + "): it is now READY.");
        }
        return ActionResult.success("Chef prepared unit " + progress + " of order #" + orderId + " ("
                + order.getItem().getName() + ").");
    }

    @Override
    public boolean hasPossibleTask(Restaurant restaurant, int turn) {
        return !restaurant.getKitchenOrders().isEmpty();
    }

    @Override
    public String describeAvailableTasks(Restaurant restaurant, int turn) {
        int count = restaurant.getKitchenOrders().size();
        return count == 0 ? "no orders to prepare yet"
                : "advance one of " + count + " order" + (count == 1 ? "" : "s") + " in the kitchen";
    }
}
