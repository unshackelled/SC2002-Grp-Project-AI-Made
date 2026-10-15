package restaurantrush.staff;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Restaurant;

/**
 * Seats at most one queued customer per turn, in addition to the Waiter's
 * task. The Host uses the same seating rule as the Waiter.
 */
public class Host extends Staff {

    public Host(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Host";
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

    @Override
    public boolean hasPossibleTask(Restaurant restaurant, int turn) {
        return restaurant.hasFreeTable() && !restaurant.getWaitingQueue().isEmpty();
    }

    @Override
    public String describeAvailableTasks(Restaurant restaurant, int turn) {
        return hasPossibleTask(restaurant, turn)
                ? "seat one of " + restaurant.getWaitingQueue().size() + " queued customer(s)"
                : "nobody to seat (needs a queued customer and a free table)";
    }
}
