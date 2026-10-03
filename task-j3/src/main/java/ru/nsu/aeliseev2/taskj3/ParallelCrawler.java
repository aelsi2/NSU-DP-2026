package ru.nsu.aeliseev2.taskj3;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A parallel HTTP/JSON crawler.
 */
public class ParallelCrawler {
    private final ExecutorService executor;
    private final Semaphore semaphore;
    private final HttpClient httpClient;
    private final HttpResponse.BodyHandler<String> responseBodyHandler;
    private final Gson gson;

    /**
     * Creates a new instance of {@code ParallelCrawler}.
     *
     * @param maxParallelRequests The maximum number of parallel HTTP requests the crawler is
     *                            allowed to make.
     */
    public ParallelCrawler(int maxParallelRequests) {
        if (maxParallelRequests < 1) {
            throw new IllegalArgumentException(
                "Maximum number of parallel requests must be positive");
        }
        executor = Executors.newVirtualThreadPerTaskExecutor();
        semaphore = new Semaphore(maxParallelRequests);
        httpClient = HttpClient.newBuilder().executor(executor).build();
        responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        gson = new Gson();
    }

    /**
     * Crawls the pseudo-website, starting at the specified URI.
     *
     * @param rootUri The URI to begin crawling at.
     * @return The list of messages obtained by crawling, sorted alphabetically.
     */
    public List<String> crawl(URI rootUri) {
        List<String> messages = new ArrayList<>();
        List<String> synchronizedMessages = Collections.synchronizedList(messages);
        try {
            executor.submit(() -> scanUri(rootUri, synchronizedMessages)).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.err.println("Sorting messages...");
        Collections.sort(messages);
        System.err.println("Crawling complete.");
        return messages;
    }

    private void scanUri(URI uri, List<String> messages) {
        try {
            HttpResponse<String> response;

            semaphore.acquire();
            try {
                System.err.println("Scanning: " + uri);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
                response = httpClient.send(request, responseBodyHandler);
            } finally {
                semaphore.release();
            }

            if (response.statusCode() != 200) {
                System.err.println("Http code" + response.statusCode() + ": " + uri);
                return;
            }

            ResponseBody responseData = gson.fromJson(response.body(),
                ResponseBody.class);
            messages.add(responseData.message);
            List<Callable<Void>> childTasks = responseData.successors.stream()
                .map(successor -> (Callable<Void>) () -> {
                    URI successorUri;
                    try {
                        successorUri = new URI(
                            uri.getScheme(), uri.getAuthority(), "/" + successor, null, null);
                    }
                    catch (RuntimeException e) {
                        System.err.println("URI error: " + e);
                        return null;
                    }
                    scanUri(successorUri, messages);
                    return null;
                }).toList();
            executor.invokeAll(childTasks);
        } catch (InterruptedException e) {
            // just return normally
        } catch (IOException e) {
            System.err.println("I/O error: " + uri);
            System.err.println(e + ": " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Error: " + uri);
            System.err.println(e + ": " + e.getMessage());
        }
    }

    private record ResponseBody(String message, ArrayList<String> successors) {
    }
}
