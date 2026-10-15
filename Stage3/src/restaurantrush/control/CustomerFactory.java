package restaurantrush.control;

import restaurantrush.model.Customer;

/** Creates the customer who arrives at a scheduled turn, e.g. {@code RegularCustomer::new}. */
@FunctionalInterface
public interface CustomerFactory {

    Customer create(int id, int arrivalTurn);
}
