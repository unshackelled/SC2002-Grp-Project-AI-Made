package restaurantrush.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

/**
 * Reads and validates console input, asking again until the input is valid.
 * Lines starting with '#' are ignored so that scripted test files can carry
 * comments. With echo on, every accepted line is printed, which makes logs of
 * piped-in runs readable.
 */
public class InputReader {

    private final BufferedReader in;
    private final boolean echo;

    public InputReader(InputStream stream, boolean echo) {
        this.in = new BufferedReader(new InputStreamReader(stream));
        this.echo = echo;
    }

    /** Reads a whole number in [min, max], asking again until one is given. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + " [" + min + "-" + max + "]: ");
            String line = nextLine();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  [!] Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] '" + line + "' is not a whole number. Please try again.");
            }
        }
    }

    private String nextLine() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    System.out.println();
                    throw new InputEndedException();
                }
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                if (echo) {
                    System.out.println(line);
                }
                return line;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
