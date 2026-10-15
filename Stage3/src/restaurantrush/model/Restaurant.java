package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The dining room. It owns the tables, the waiting queue, every customer and
 * order seen this game, and the scoreboard. Seating, ordering and departures go
 * through here so that the queue and tables always agree.
 */
public class Restaurant {

    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final Scoreboard scoreboard = new Scoreboard();

    public Restaurant(int tableCount) {
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }

    /** A new arrival joins the back of the waiting queue. */
    public void admit(Customer customer) {
        customers.add(customer);
        waitingQueue.add(customer);
    }

    /** Seats a queued customer at the lowest-numbered free table. */
    public ActionResult seatFromQueue(int customerId) {
        Optional<Customer> found = findCustomer(customerId);
        if (!found.isPresent()) {
            return ActionResult.failure("There is no customer C" + customerId + ".");
        }
        Customer customer = found.get();
        if (customer.getStatus() != CustomerStatus.WAITING) {
            return ActionResult.failure(customer.getLabel() + " is not in the waiting queue ("
                    + customer.getStatus().getLabel() + ").");
        }
        Optional<Table> table = findFreeTable();
        if (!table.isPresent()) {
            return ActionResult.failure("No table is free. A table stays occupied until its customer pays or leaves.");
        }
        waitingQueue.remove(customer);
        customer.seatAt(table.get());
        return ActionResult.success(customer.getLabel() + " is seated at Table " + table.get().getNumber() + ".");
    }

    /** Records a new order for a seated customer. */
    public Order placeOrder(Customer customer, MenuItem item, int billCents, int turn) {
        Order order = new Order(customer.getId(), customer, item, billCents, turn);
        customer.placeOrder(order);
        orders.add(order);
        return order;
    }

    /** A customer whose satisfaction reached 0 leaves without paying. */
    public void handleUnhappyDeparture(Customer customer) {
        waitingQueue.remove(customer);
        if (customer.getOrder() != null) {
            customer.getOrder().cancel();
        }
        customer.leave();
        scoreboard.recordUnhappyDeparture();
    }

    // ----- queries -----

    public Optional<Customer> findCustomer(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Order> findOrder(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    public Optional<Table> findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst();
    }

    public boolean hasFreeTable() {
        return findFreeTable().isPresent();
    }

    public List<Customer> getCustomersWithStatus(CustomerStatus status) {
        return customers.stream().filter(c -> c.getStatus() == status).collect(Collectors.toList());
    }

    public List<Order> getKitchenOrders() {
        return orders.stream().filter(o -> o.getStatus().isInKitchen()).collect(Collectors.toList());
    }

    public List<Order> getServableOrders(int turn) {
        return orders.stream().filter(o -> o.isServableOn(turn)).collect(Collectors.toList());
    }

    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
