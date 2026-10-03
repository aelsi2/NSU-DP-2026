package ru.nsu.aeliseev2.taskj3;

import java.util.List;

class Application {
    private static void printHelp() {
        System.err.print("""
            Usage:
            task-j3 <ROOT-URI> [MAX-PARALLEL-REQUESTS]
            
            Options:
                ROOT-URI                - root URI, starting point for crawling
                MAX-PARALLEL-REQUESTS   - maximum number of parallel HTTP requests to the server
                                          default: 100
            """);
    }

    public static void main(String[] args) {
        try {
            ParsedArgs parsedArgs = ParsedArgs.from(args);
            ParallelCrawler crawler = new ParallelCrawler(parsedArgs.maxParallelRequests);
            List<String> messages = crawler.crawl(parsedArgs.rootUri);
            for (String message : messages) {
                System.out.println(message);
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
