package ru.nsu.aeliseev2.taskj1.utils;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class for parsing server addresses.
 */
public final class AddressParser {
    private AddressParser() {
    }

    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
        "^(?:\\[(?<ipv6>[a-fA-F0-9:]+)]|(?<host>[a-zA-Z0-9_.-]+)):(?<port>[0-9]+)$"
    );

    /**
     * Parses the address from a string.
     *
     * @param string The string to parse from.
     * @return The parsed address.
     */
    public static InetSocketAddress parse(String string) {
        Matcher matcher = ADDRESS_PATTERN.matcher(string);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid address string: " + string);
        }
        String hostname;
        if (matcher.group("ipv6") != null) {
            hostname = matcher.group("ipv6");
        } else {
            hostname = matcher.group("host");
        }
        int port = Port.parse(matcher.group("port"));
        return new InetSocketAddress(hostname, port);
    }
}
