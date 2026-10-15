package restaurantrush;

import java.util.Arrays;

import restaurantrush.ui.ConsoleUI;
import restaurantrush.ui.InputEndedException;
import restaurantrush.ui.InputReader;

/**
 * Entry point. Pass {@code --echo} when piping in a test script so that the
 * scripted inputs appear in the output.
 */
public class Main {

    public static void main(String[] args) {
        boolean echo = Arrays.asList(args).contains("--echo");
        try {
            new ConsoleUI(new InputReader(System.in, echo)).run();
        } catch (InputEndedException e) {
            System.out.println("Input ended - exiting.");
        }
    }
}
