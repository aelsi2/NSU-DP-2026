package ru.nsu.aeliseev2.taskj1.utils;

/**
 * A helper class for parsing port numbers.
 */
public final class Port {
    private Port() {
    }

    /**
     * Parses a port number from a string.
     *
     * @param string The string to parse from.
     * @return The parsed port number.
     */
    public static int parse(String string) {
        int port;
        try {
            port = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port number: " + string, e);
        }
        if (port < 0 || port > Short.toUnsignedInt((short) -1)) {
            throw new IllegalArgumentException("Port number out of range: " + string);
        }
        return port;
    }
}
