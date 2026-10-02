package ru.nsu.aeliseev2.taskj2;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

class Application {
    private static void printHelp() {
        System.err.print("""
            Usage:
            sorter <TYPE> [NUM-THREADS] [OUT-DELAY] [IN-DELAY]
            
            Options:
                TYPE          - list type: either "LINKED" or "ARRAY"
                NUM-THREADS   - number of sorting threads (positive integer), default: 1
                OUT-DELAY     - delay between sorting steps (when mutexes are not held) in ms, default: 1000
                IN-DELAY      - delay inside each sorting step (when mutexes are held) in ms, default: 0
            """);
    }

    private synchronized static void printList(Iterable<String> list) {
        for (String element : list) {
            System.out.println(" - " + element);
        }
        System.out.println("----------");
    }

    private static void startSorter(int i, ConcurrentlySortedStringList list,
                                    int outDelayMs, int inDelayMs) {
        Thread sorter = new Thread(() -> {
            try {
                ConcurrentlySortedStringList.Sorterator sorterator = list.sort();
                while (true) {
                    boolean changed = sorterator.next(inDelayMs);
                    if (changed && outDelayMs != 0) {
                        printList(list);
                    }
                    if (outDelayMs != 0) {
                        if (outDelayMs < 0) {
                            Thread.sleep(ThreadLocalRandom.current().nextInt(0, -outDelayMs));
                        } else {
                            Thread.sleep(outDelayMs);
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Worker " + i + " interrupted");
            }
        });
        sorter.setDaemon(true);
        sorter.start();
    }

    private static void readStrings(ConcurrentlySortedStringList list) {
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (line.isEmpty()) {
                printList(list);
            } else {
                list.add(line);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ParsedArgs parsedArgs = ParsedArgs.from(args);
            ConcurrentlySortedStringList list = switch (parsedArgs.listType) {
                case LINKED -> new ConcurrentlySortedLinkedList();
                case ARRAY -> new ConcurrentlySortedArrayList();
            };
            for (int i = 0; i < parsedArgs.numThreads; i++) {
                startSorter(i, list, parsedArgs.outDelayMs, parsedArgs.inDelayMs);
            }
            System.out.println("Started " + parsedArgs.numThreads + " threads");
            readStrings(list);
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
