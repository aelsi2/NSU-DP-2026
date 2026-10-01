package ru.nsu.aeliseev2.taskj1.keyclient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import ru.nsu.aeliseev2.taskj1.utils.AddressParser;
import ru.nsu.aeliseev2.taskj1.utils.KeyCertPair;

class Application {
    private static void printHelp() {
        System.err.print("""
            Usage:
            key-client <DATA IP>:<DATA PORT> <CLIENT CN> [DELAY] [ERROR]
            """);
    }

    private static void writeFiles(String name, KeyCertPair pair) throws IOException {
       try (BufferedWriter crtFile = Files.newBufferedWriter(Path.of(name + ".crt"))) {
           crtFile.write(PemEncoding.convert(pair.cert(), "CERTIFICATE"));
       }
       try (BufferedWriter keyFile = Files.newBufferedWriter(Path.of(name + ".key"))) {
           keyFile.write(PemEncoding.convert(pair.privateKey(), "PRIVATE KEY"));
       }
    }

    public static void main(String[] args) {
        try  {
            if (args.length < 2 || args.length > 4) {
                throw new IllegalArgumentException("Unexpected number of arguments");
            }
            InetSocketAddress dataAddress = AddressParser.parse(args[0]);
            String name = args[1];
            int delay = 0;
            boolean error = false;
            if (args.length >= 3) {
                try {
                    delay = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    delay = -1;
                }
                if (delay < 0) {
                    throw new IllegalArgumentException(
                        "Delay must be a non-negative integer.");
                }
            }
            if (args.length == 4) {
                if (args[3].equals("ERROR")) {
                    error = true;
                } else {
                    throw new IllegalArgumentException(
                        "The optional last argument must be \"ERROR\".");
                }
            }
            KeyCertPair pair = ClientConnection.getKeyCertPair(dataAddress, name, delay, error);
            writeFiles(name, pair);
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
