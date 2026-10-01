package ru.nsu.aeliseev2.taskj1.keyserver;

import java.net.InetSocketAddress;
import ru.nsu.aeliseev2.taskj1.utils.AddressParser;

class Application {
    private static void printHelp() {
        System.err.print("""
            Usage:
            key-server <DATA IP>:<DATA PORT> <X.509 ISSUER CN> [NUMBER OF THREADS]
            """);
    }

    public static void main(String[] args) {
        try  {
            if (args.length < 2 || args.length > 3) {
                throw new IllegalArgumentException("Unexpected number of arguments");
            }
            InetSocketAddress dataAddress = AddressParser.parse(args[0]);
            int numThreads = Runtime.getRuntime().availableProcessors();
            if (args.length > 2) {
                try {
                    numThreads = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    numThreads = -1;
                }
                if (numThreads < 1) {
                    throw new IllegalArgumentException(
                        "Number of threads must be a positive integer.");
                }
            }
            KeyDatabase keyDatabase = new KeyDatabase(numThreads, args[1]);
            try (KeyServer server = new KeyServer(keyDatabase)) {
                server.listen(dataAddress);
            }
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            printHelp();
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
