package ru.nsu.aeliseev2.taskj1.keyserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A server serving public+private key pairs to clients.
 */
class KeyServer implements AutoCloseable {
    private final HashMap<SelectionKey, ServerConnection> connections;
    private final KeyDatabase keyDatabase;

    /**
     * Initializes a new instance of {@code KeyServer}.
     *
     * @param keyDatabase The key database to retrieve the client keys from.
     */
    public KeyServer(KeyDatabase keyDatabase) {
        this.keyDatabase = keyDatabase;
        this.connections = new HashMap<>();
    }

    /**
     * Begins listening for incoming connections and servicing them. Blocks indefinitely.
     *
     * @param dataAddress The address to listen for connections on.
     * @throws IOException Selector error.
     */
    public void listen(InetSocketAddress dataAddress) throws IOException {
        try (
            ServerSocketChannel acceptChannel = ServerSocketChannel.open().bind(dataAddress);
            Selector selector = Selector.open()
        ) {
            acceptChannel.configureBlocking(false);
            acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.err.println("Listening for connections on TCP " + dataAddress);
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        acceptClient(key);
                    } else {
                        handleOps(key);
                    }
                    iterator.remove();
                }
            }

        }
    }

    /**
     * Terminates the connection.
     *
     * @param connection The connection to terminate.
     */
    private void disconnectClient(ServerConnection connection) {
        connections.remove(connection.key());
        connection.close();
    }

    /**
     * Accepts a new client.
     *
     * @param key The server socket key.
     */
    private void acceptClient(SelectionKey key) {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            ServerConnection connection =
                new ServerConnection(clientSocket, key.selector(), keyDatabase);
            System.err.println("Incoming connection: " + clientSocket.getRemoteAddress());
            connections.put(connection.key(), connection);
        } catch (IOException exception) {
            System.err.println("Accept failed.");
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                    System.err.println("Close failed: " + ioException.getMessage());
                }
            }
        }
    }

    /**
     * Handles read/write operations on a client.
     *
     * @param key The client key.
     */
    private void handleOps(SelectionKey key) {
        ServerConnection connection = connections.get(key);
        try {
            if (connection.handleOps()) {
                System.err.println("Client disconnected.");
                disconnectClient(connection);
            }
        } catch (Exception exception) {
            System.err.println("Connection error: " + exception.getMessage());
            disconnectClient(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        for (ServerConnection connection : connections.values()) {
            connection.close();
        }
    }
}
