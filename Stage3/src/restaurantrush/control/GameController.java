package restaurantrush.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Customer;
import restaurantrush.model.HappyHour;
import restaurantrush.model.Restaurant;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Host;
import restaurantrush.staff.Staff;
import restaurantrush.staff.Waiter;

/**
 * Runs the turn sequence and applies the game rules. The UI calls
 * {@link #startTurn()}, passes on the manager's staff choices, then calls
 * {@link #endTurn()}. Everything that happens without a manager decision is
 * done here and reported through the event log.
 */
public class GameController {

    private final GameConfig config;
    private final Restaurant restaurant;
    private final HappyHour happyHour = new HappyHour();
    private final Host host = new Host("Hana");
    private final Waiter waiter = new Waiter("Wendy");
    private final Chef chef = new Chef("Carlos");
    private final Cashier cashier = new Cashier("Casey");
    private final List<Staff> staff;
    private final List<String> events = new ArrayList<>();
    private int turn;
    private int nextCustomerId = 1;

    public GameController(GameConfig config) {
        this.config = config;
        this.restaurant = new Restaurant(config.getTableCount());
        this.staff = Arrays.asList(host, waiter, chef, cashier);
    }

    // ----- Step 1: start of turn and arrivals -----

    public void startTurn() {
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over.");
        }
        turn++;
        for (Staff member : staff) {
            member.startTurn();
        }
        config.getSchedule().arrivalAt(turn).ifPresent(factory -> {
            Customer customer = factory.create(nextCustomerId++, turn);
            restaurant.admit(customer);
            log("Arrival: " + customer.getLabel() + " joins the waiting queue with satisfaction "
                    + customer.getSatisfaction() + ".");
        });
        if (happyHour.isActive()) {
            applyHappyHourRecovery();
        }
    }

    // ----- Happy Hour: may be switched on once, at the start of a turn -----

    public ActionResult activateHappyHour() {
        if (!happyHour.isAvailable()) {
            return ActionResult.failure("Happy Hour has already been used; it can be activated once per game.");
        }
        for (Staff member : staff) {
            if (member.hasActed()) {
                return ActionResult.failure("Happy Hour can only be switched on at the start of a turn.");
            }
        }
        happyHour.activate();
        applyHappyHourRecovery();
        return ActionResult.success("Happy Hour is ON for this turn and the next: orders taken get "
                + (100 - HappyHour.BILL_PERCENT) + "% off.");
    }

    private void applyHappyHourRecovery() {
        List<String> changes = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.isWaitingForService()) {
                int before = customer.getSatisfaction();
                customer.recover(HappyHour.RECOVERY);
                changes.add("C" + customer.getId() + " " + before + "->" + customer.getSatisfaction());
            }
        }
        if (!changes.isEmpty()) {
            log("Happy Hour recovery (+" + HappyHour.RECOVERY + "): " + String.join(", ", changes));
        }
    }

    public boolean isHappyHourAvailable() {
        return happyHour.isAvailable();
    }

    public boolean isHappyHourActive() {
        return happyHour.isActive();
    }

    public int getHappyHourTurnsLeft() {
        return happyHour.getRemainingTurns();
    }

    // ----- Steps 2 and 3: manager choices; Host, then Waiter, then Chef -----

    public ActionResult hostSeat(int customerId) {
        return host.seat(restaurant, customerId);
    }

    public ActionResult hostWait() {
        return host.idle();
    }

    public boolean hostHasPossibleTask() {
        return host.hasPossibleTask(restaurant, turn);
    }

    public ActionResult waiterSeat(int customerId) {
        return waiter.seat(restaurant, customerId);
    }

    public ActionResult waiterTakeOrder(int customerId) {
        return waiter.takeOrder(restaurant, customerId, config.getMenu(), happyHour, turn);
    }

    public ActionResult waiterServe(int orderId) {
        return waiter.serve(restaurant, orderId, turn);
    }

    public ActionResult waiterWait() {
        return waiter.idle();
    }

    public ActionResult chefPrepare(int orderId) {
        if (!waiter.hasActed()) {
            return ActionResult.failure("The Waiter acts before the Chef: choose the Waiter's task first.");
        }
        return chef.prepare(restaurant, orderId, turn);
    }

    public ActionResult chefWait() {
        return chef.idle();
    }

    public boolean waiterHasPossibleTask() {
        return waiter.hasPossibleTask(restaurant, turn);
    }

    public boolean chefHasPossibleTask() {
        return chef.hasPossibleTask(restaurant, turn);
    }

    // ----- Steps 4 and 5: eating, payment, waiting and abandonment -----

    public void endTurn() {
        if (!host.hasActed()) {
            host.idle();
        }
        if (!waiter.hasActed()) {
            waiter.idle();
        }
        if (!chef.hasActed()) {
            chef.idle();
        }
        handleEating();
        handlePayment();
        handleWaiting();
        happyHour.endTurn();
    }

    private void handleEating() {
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.finishEating(turn)) {
                log(customer.getLabel() + " finished eating and is READY_TO_PAY (pays next turn at the earliest).");
            }
        }
    }

    private void handlePayment() {
        ActionResult payment = cashier.collectPayment(restaurant, turn);
        if (payment.isSuccess()) {
            log(payment.getMessage());
        }
    }

    private void handleWaiting() {
        List<String> changes = new ArrayList<>();
        List<Customer> leaving = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (!customer.isWaitingForService()) {
                continue;
            }
            int before = customer.getSatisfaction();
            customer.loseSatisfactionForWaiting();
            changes.add("C" + customer.getId() + " " + before + "->" + customer.getSatisfaction());
            if (customer.getSatisfaction() == 0) {
                leaving.add(customer);
            }
        }
        if (!changes.isEmpty()) {
            log("Waiting penalty: " + String.join(", ", changes));
        }
        for (Customer customer : leaving) {
            String where = customer.getTable() != null
                    ? " Table " + customer.getTable().getNumber() + " is free." : "";
            String cancelled = customer.getOrder() != null
                    ? " Order #" + customer.getOrder().getId() + " cancelled." : "";
            restaurant.handleUnhappyDeparture(customer);
            log(customer.getLabel() + " reached 0 satisfaction and LEFT without paying." + cancelled + where);
        }
    }

    // ----- Step 6: result -----

    public boolean isGameOver() {
        return turn >= config.getMaxTurns();
    }

    public boolean isVictory() {
        return isGameOver() && config.getObjective().isMet(restaurant.getScoreboard());
    }

    // ----- queries for the UI -----

    /** Returns and clears the events logged since the last call. */
    public List<String> drainEvents() {
        List<String> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    public List<String> describeStaffTasks() {
        List<String> lines = new ArrayList<>();
        for (Staff member : staff) {
            lines.add(String.format("%-8s: %s", member.getRole(), member.describeAvailableTasks(restaurant, turn)));
        }
        return lines;
    }

    public OptionalInt getNextArrivalTurn() {
        return config.getSchedule().nextArrivalAfter(turn);
    }

    public int getTurn() {
        return turn;
    }

    public int getMaxTurns() {
        return config.getMaxTurns();
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public GameConfig getConfig() {
        return config;
    }

    private void log(String message) {
        events.add(message);
    }
}
