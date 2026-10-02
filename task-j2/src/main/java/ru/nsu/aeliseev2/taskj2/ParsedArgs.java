package ru.nsu.aeliseev2.taskj2;

/**
 * Contains the parsed command line arguments passed to the application.
 */
class ParsedArgs {
    /**
     * List implementation to use.
     */
    public ListType listType;

    /**
     * Number of sorting threads.
     */
    public int numThreads = 1;

    /**
     * Delay in milliseconds between sorting steps.
     */
    public int outDelayMs = 1000;

    /**
     * Delay in milliseconds inside each sorting step.
     */
    public int inDelayMs = 0;

    /**
     * List implementation identifier.
     */
    public enum ListType {
        /**
         * Corresponds to {@code ConcurrentlySortedLinkedList}.
         */
        LINKED,
        /**
         * Corresponds to {@code ConcurrentlySortedArrayList}.
         */
        ARRAY,
    }

    private ParsedArgs(String[] args) {
        if (args.length < 1 || args.length > 4) {
            throw new IllegalArgumentException("Unexpected number of arguments");
        }
        switch (args[0].toUpperCase()) {
            case "LINKED":
                listType = ListType.LINKED;
                break;
            case "ARRAY":
                listType = ListType.ARRAY;
                break;
            default:
                throw new IllegalArgumentException(
                    "List type must either be \"LINKED\" or \"ARRAY\".");
        }
        if (args.length >= 2) {
            try {
                numThreads = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                numThreads = -1;
            }
            if (numThreads < 1) {
                throw new IllegalArgumentException(
                    "Number of threads must be a positive integer.");
            }
        }
        if (args.length >= 3) {
            try {
                outDelayMs = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "OUT-DELAY must be an integer.", e);
            }
        }
        if (args.length >= 4) {
            try {
                inDelayMs = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                inDelayMs = -1;
            }
            if (inDelayMs < 0) {
                throw new IllegalArgumentException(
                    "IN-DELAY must be a nonnegative integer.");
            }
        }
    }

    /**
     * Parses an array of command arguments and creates an instance of {@code ParsedArgs}.
     *
     * @param args The raw command line arguments.
     */
    public static ParsedArgs from(String[] args) {
        return new ParsedArgs(args);
    }
}
