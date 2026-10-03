package ru.nsu.aeliseev2.taskj3;

import java.net.URI;

/**
 * Contains the parsed command line arguments passed to the application.
 */
class ParsedArgs {
    /**
     * Root URI to start crawling at.
     */
    public final URI rootUri;

    /**
     * Maximum number of parallel HTTP requests the crawler is allowed to make.
     */
    public final int maxParallelRequests;

    private ParsedArgs(URI rootUri, int maxParallelRequests) {
        this.rootUri = rootUri;
        this.maxParallelRequests = maxParallelRequests;
    }

    /**
     * Parses an array of command arguments and creates an instance of {@code ParsedArgs}.
     *
     * @param args The raw command line arguments.
     */
    public static ParsedArgs from(String[] args) {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Unexpected number of arguments");
        }

        URI uri = URI.create(args[0]);
        int maxParallelRequests = 100;

        if (args.length >= 2) {
            try {
                maxParallelRequests = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                maxParallelRequests = -1;
            }
            if (maxParallelRequests < 0) {
                throw new IllegalArgumentException(
                    "MAX-PARALLEL-REQUESTS must be a positive integer");
            }
        }
        return new ParsedArgs(uri, maxParallelRequests);
    }
}
