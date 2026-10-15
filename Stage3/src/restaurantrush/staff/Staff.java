package restaurantrush.staff;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Restaurant;

/**
 * A staff member who can do at most one task per turn. Choosing to wait also
 * uses up the turn. Each role decides for itself what work is available.
 */
public abstract class Staff {

    private final String name;
    private boolean taskUsed;

    protected Staff(String name) {
        this.name = name;
    }

    /** Role name shown to the player, e.g. "Waiter". */
    public abstract String getRole();

    /** True if this staff member has at least one valid task right now. */
    public abstract boolean hasPossibleTask(Restaurant restaurant, int turn);

    /** Short summary of the work available to this staff member right now. */
    public abstract String describeAvailableTasks(Restaurant restaurant, int turn);

    public void startTurn() {
        taskUsed = false;
    }

    public boolean hasActed() {
        return taskUsed;
    }

    /** The manager chooses to let this staff member do nothing this turn. */
    public ActionResult idle() {
        if (taskUsed) {
            return alreadyActed();
        }
        taskUsed = true;
        return ActionResult.success(getRole() + " " + name + " waits this turn.");
    }

    protected void completeTask() {
        taskUsed = true;
    }

    protected ActionResult alreadyActed() {
        return ActionResult.failure(getRole() + " " + name + " has already completed a task this turn.");
    }

    public String getName() {
        return name;
    }
}
