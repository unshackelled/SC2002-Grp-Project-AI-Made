package restaurantrush.model;

/** A dining table. It stays occupied until its customer pays or leaves. */
public class Table {

    private final int number;
    private Customer occupant;

    public Table(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public Customer getOccupant() {
        return occupant;
    }

    void assign(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is already occupied");
        }
        occupant = customer;
    }

    void vacate() {
        occupant = null;
    }
}
