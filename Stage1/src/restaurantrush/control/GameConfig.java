package restaurantrush.control;

import restaurantrush.model.Dish;
import restaurantrush.model.Menu;
import restaurantrush.model.RegularCustomer;

/** Fixed parameters of one game. {@link #baseSetting()} is the required, reproducible setting. */
public class GameConfig {

    private final int maxTurns;
    private final int tableCount;
    private final Menu menu;
    private final ArrivalSchedule schedule;
    private final Objective objective;

    public GameConfig(int maxTurns, int tableCount, Menu menu, ArrivalSchedule schedule, Objective objective) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = menu;
        this.schedule = schedule;
        this.objective = objective;
    }

    /** 18 turns, 2 tables, six arrivals, the three-dish menu and the standard objective. */
    public static GameConfig baseSetting() {
        Menu menu = new Menu();
        menu.add(new Dish("Salad", 900, 1));
        menu.add(new Dish("Burger", 1500, 1));
        menu.add(new Dish("Pasta", 1800, 2));

        ArrivalSchedule schedule = new ArrivalSchedule()
                .add(1, RegularCustomer::new)
                .add(4, RegularCustomer::new)
                .add(7, RegularCustomer::new)
                .add(10, RegularCustomer::new)
                .add(13, RegularCustomer::new)
                .add(16, RegularCustomer::new);

        return new GameConfig(18, 2, menu, schedule, new Objective(4, 4500, 60));
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public Menu getMenu() {
        return menu;
    }

    public ArrivalSchedule getSchedule() {
        return schedule;
    }

    public Objective getObjective() {
        return objective;
    }
}
