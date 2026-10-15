package restaurantrush.ui;

import java.util.List;
import java.util.OptionalInt;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.Objective;
import restaurantrush.model.ActionResult;
import restaurantrush.model.Customer;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Scoreboard;
import restaurantrush.model.Table;

/**
 * Command-line boundary. It only collects the manager's choices and displays
 * the game state; every rule lives in the control and model classes.
 */
public class ConsoleUI {

    private static final String LINE = "======================================================================";
    private static final int MAX_ID = 999;

    private final InputReader input;

    public ConsoleUI(InputReader input) {
        this.input = input;
    }

    public void run() {
        System.out.println(LINE);
        System.out.println("  RESTAURANT RUSH  -  SC2002 turn-based restaurant manager (Stage 1)");
        System.out.println(LINE);
        while (true) {
            System.out.println();
            System.out.println("Main menu:  1) Play (base setting)   2) How to play   0) Quit");
            int choice = input.readInt("Choose", 0, 2);
            if (choice == 0) {
                System.out.println("Goodbye!");
                return;
            }
            if (choice == 2) {
                printRules(GameConfig.baseSetting());
            } else {
                play(new GameController(GameConfig.baseSetting()));
            }
        }
    }

    // ----- game loop -----

    private void play(GameController game) {
        while (!game.isGameOver()) {
            game.startTurn();
            System.out.println();
            System.out.println(LINE);
            System.out.printf("  TURN %d / %d%n", game.getTurn(), game.getMaxTurns());
            System.out.println(LINE);
            printEvents(game.drainEvents());
            printStatus(game);
            waiterPhase(game);
            chefPhase(game);
            game.endTurn();
            System.out.println();
            System.out.println("-- End of turn " + game.getTurn() + " --");
            printEvents(game.drainEvents());
            printEndOfTurn(game);
        }
        printFinalResult(game);
    }

    private void waiterPhase(GameController game) {
        System.out.println();
        if (!game.waiterHasPossibleTask()) {
            report(game.waiterWait());
            System.out.println("  (no Waiter task is possible this turn)");
            return;
        }
        while (true) {
            System.out.println("Waiter task:  1) Seat a queued customer   2) Take an order   3) Serve a READY order   0) Wait");
            int choice = input.readInt("Waiter", 0, 3);
            ActionResult result;
            if (choice == 0) {
                result = game.waiterWait();
            } else {
                String prompt = choice == 1 ? "  Customer number to seat (0 = back)"
                        : choice == 2 ? "  Customer number to take the order from (0 = back)"
                        : "  Order number to serve (0 = back)";
                int id = input.readInt(prompt, 0, MAX_ID);
                if (id == 0) {
                    continue;
                }
                result = choice == 1 ? game.waiterSeat(id)
                        : choice == 2 ? game.waiterTakeOrder(id)
                        : game.waiterServe(id);
            }
            report(result);
            if (result.isSuccess()) {
                return;
            }
        }
    }

    private void chefPhase(GameController game) {
        System.out.println();
        if (!game.chefHasPossibleTask()) {
            report(game.chefWait());
            System.out.println("  (no order is waiting in the kitchen)");
            return;
        }
        printKitchen(game.getRestaurant());
        while (true) {
            int id = input.readInt("Chef: order number to advance by one unit (0 = Wait)", 0, MAX_ID);
            ActionResult result = id == 0 ? game.chefWait() : game.chefPrepare(id);
            report(result);
            if (result.isSuccess()) {
                return;
            }
        }
    }

    // ----- display -----

    private void printStatus(GameController game) {
        Restaurant restaurant = game.getRestaurant();
        Scoreboard score = restaurant.getScoreboard();
        System.out.printf("Revenue: %s | Avg satisfaction (paid): %s | Paid: %d | Served: %d | Left unhappy: %d%n",
                Money.format(score.getRevenueCents()), formatAverage(score), score.getPaidCount(),
                score.getServedCount(), score.getUnhappyDepartures());
        OptionalInt next = game.getNextArrivalTurn();
        System.out.println(next.isPresent() ? "Next arrival: turn " + next.getAsInt() : "No more arrivals.");

        System.out.println();
        System.out.println("Waiting queue:");
        if (restaurant.getWaitingQueue().isEmpty()) {
            System.out.println("  (empty)");
        }
        for (Customer customer : restaurant.getWaitingQueue()) {
            System.out.printf("  %-14s satisfaction %3d  (-%d per waiting turn)%n", customer.getLabel(),
                    customer.getSatisfaction(), customer.getSatisfactionLoss());
        }

        System.out.println("Tables:");
        for (Table table : restaurant.getTables()) {
            Customer customer = table.getOccupant();
            if (customer == null) {
                System.out.printf("  Table %d: (free)%n", table.getNumber());
            } else {
                System.out.printf("  Table %d: %-14s satisfaction %3d  %-26s %s%n", table.getNumber(),
                        customer.getLabel(), customer.getSatisfaction(), customer.getStatus().getLabel(),
                        customer.getOrder() == null ? "no order yet" : formatOrder(customer.getOrder()));
            }
        }

        System.out.println("Staff tasks available:");
        for (String line : game.describeStaffTasks()) {
            System.out.println("  " + line);
        }
    }

    private void printKitchen(Restaurant restaurant) {
        System.out.println("Kitchen:");
        for (Order order : restaurant.getKitchenOrders()) {
            System.out.println("  " + formatOrder(order));
        }
    }

    private void printEndOfTurn(GameController game) {
        Scoreboard score = game.getRestaurant().getScoreboard();
        System.out.printf("Served: %d | Unhappy departures: %d | Revenue: %s | Avg satisfaction (paid): %s | Turns used: %d/%d%n",
                score.getServedCount(), score.getUnhappyDepartures(), Money.format(score.getRevenueCents()),
                formatAverage(score), game.getTurn(), game.getMaxTurns());
    }

    private void printFinalResult(GameController game) {
        Scoreboard score = game.getRestaurant().getScoreboard();
        Objective objective = game.getConfig().getObjective();
        System.out.println();
        System.out.println(LINE);
        System.out.println("  GAME OVER after " + game.getTurn() + " turns");
        System.out.println(LINE);
        System.out.printf("  Paid customers      : %-8d target >= %-7d %s%n", score.getPaidCount(),
                objective.getMinPaidCustomers(), metLabel(objective.isPaidCustomersMet(score)));
        System.out.printf("  Revenue             : %-8s target >= %-7s %s%n", Money.format(score.getRevenueCents()),
                Money.format(objective.getMinRevenueCents()), metLabel(objective.isRevenueMet(score)));
        System.out.printf("  Average satisfaction: %-8s target >= %-7d %s%n", formatAverage(score),
                objective.getMinAverageSatisfaction(), metLabel(objective.isSatisfactionMet(score)));
        System.out.printf("  Served: %d | Unhappy departures: %d%n", score.getServedCount(), score.getUnhappyDepartures());
        System.out.println();
        System.out.println(game.isVictory() ? "  *** VICTORY! The restaurant hit every target. ***"
                : "  *** DEFEAT. At least one target was missed. ***");
        System.out.println(LINE);
    }

    private void printRules(GameConfig config) {
        System.out.println();
        System.out.println("HOW TO PLAY");
        System.out.println("You are the manager. Each turn you choose one task for the Waiter and one for the Chef.");
        System.out.println(" - Waiter: seat a queued customer, take a seated customer's order, or serve a READY order.");
        System.out.println(" - Chef: advance one order by one preparation unit (the Waiter acts first).");
        System.out.println(" - Cashier: automatically takes one payment per turn, oldest READY_TO_PAY customer first.");
        System.out.println(" - A dish finished this turn can be served next turn. A served customer eats for one");
        System.out.println("   full turn, becomes READY_TO_PAY, and pays the turn after that.");
        System.out.println(" - Customers waiting for a table or food lose satisfaction at the end of every turn;");
        System.out.println("   at 0 they leave without paying.");
        System.out.println(" - Customers choose their own food: C1 takes menu item 1, C2 item 2, and so on (wrapping).");
        System.out.println("Menu:");
        for (MenuItem item : config.getMenu().getItems()) {
            System.out.println("   " + item);
        }
        Objective objective = config.getObjective();
        System.out.printf("Goal after %d turns: at least %d paid customers, revenue at least %s, and average%n",
                config.getMaxTurns(), objective.getMinPaidCustomers(), Money.format(objective.getMinRevenueCents()));
        System.out.printf("satisfaction of paid customers at least %d.%n", objective.getMinAverageSatisfaction());
    }

    private void printEvents(List<String> events) {
        for (String event : events) {
            System.out.println("  * " + event);
        }
    }

    private void report(ActionResult result) {
        System.out.println(result.isSuccess() ? "  -> " + result.getMessage()
                : "  [!] " + result.getMessage() + " Choose again.");
    }

    private String formatOrder(Order order) {
        return String.format("Order #%d %s %s %d/%d units, bill %s", order.getId(), order.getItem().getName(),
                order.getStatus(), order.getUnitsDone(), order.getItem().getPrepUnits(),
                Money.format(order.getBillCents()));
    }

    private String formatAverage(Scoreboard score) {
        return score.hasPaidCustomers() ? String.format("%.1f", score.getAverageSatisfaction()) : "N/A";
    }

    private String metLabel(boolean met) {
        return met ? "MET" : "NOT MET";
    }
}
