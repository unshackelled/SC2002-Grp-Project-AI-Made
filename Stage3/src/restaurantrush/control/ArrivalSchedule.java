package restaurantrush.control;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;

/** Which kind of customer arrives on which turn. */
public class ArrivalSchedule {

    private final Map<Integer, CustomerFactory> arrivals = new TreeMap<>();

    public ArrivalSchedule add(int turn, CustomerFactory factory) {
        arrivals.put(turn, factory);
        return this;
    }

    public Optional<CustomerFactory> arrivalAt(int turn) {
        return Optional.ofNullable(arrivals.get(turn));
    }

    public OptionalInt nextArrivalAfter(int turn) {
        return arrivals.keySet().stream().mapToInt(Integer::intValue).filter(t -> t > turn).findFirst();
    }

    public int size() {
        return arrivals.size();
    }
}
