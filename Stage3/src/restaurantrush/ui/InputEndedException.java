package restaurantrush.ui;

/** Thrown when standard input runs out, e.g. at the end of a scripted test file. */
public class InputEndedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputEndedException() {
        super("Input ended.");
    }
}
