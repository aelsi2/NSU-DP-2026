package ru.nsu.aeliseev2.taskj1.keyserver;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import ru.nsu.aeliseev2.taskj1.protocol.MessageReader;
import ru.nsu.aeliseev2.taskj1.protocol.MessageWriter;
import ru.nsu.aeliseev2.taskj1.protocol.ProtocolException;
import ru.nsu.aeliseev2.taskj1.protocol.messages.KeyCertMessage;
import ru.nsu.aeliseev2.taskj1.protocol.messages.NameMessage;

/**
 * A connection to a single client on a server.
 */
class ServerConnection implements Closeable {
    private static final int BUFFER_SIZE = 1024;

    private final SocketChannel channel;
    private final Selector selector;
    private final SelectionKey key;

    private final ByteBuffer sendBuffer;
    private final ByteBuffer receiveBuffer;
    private final MessageReader<NameMessage> reader;
    private final MessageWriter writer;

    private final KeyDatabase database;

    /**
     * Initializes a new instance of {@code ServerConnection}.
     *
     * @param channel  The channel to use to communicate with the client.
     * @param selector The selector to register the channel in.
     * @param database The database to retrieve keys from.
     * @throws IOException Channel configuration error.
     */
    public ServerConnection(SocketChannel channel, Selector selector, KeyDatabase database)
        throws IOException {
        channel.configureBlocking(false);
        this.channel = channel;
        this.selector = selector;
        this.database = database;
        this.key = channel.register(selector, SelectionKey.OP_READ);
        this.sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.reader = new MessageReader<>(new NameMessage.Deserializer());
        this.writer = new MessageWriter();
    }

    /**
     * Schedules an asynchronous key retrieval operation.
     *
     * @param name The name of the client to get the key pair for.
     */
    private void scheduleKeyRetrieval(String name) {
        database.getAsync(name, (n, pair) -> {
            synchronized (writer) {
                try {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                } catch (RuntimeException e) {
                    System.err.println("Could not send keys to client " + name + ": " + e);
                    return;
                }
                writer.enqueue(new KeyCertMessage(pair));
                selector.wakeup();
            }
        });
    }

    /**
     * Handles read/write operations on the connection.
     *
     * @return Whether the connection should be closed.
     * @throws IOException       Read/write error.
     * @throws ProtocolException Client protocol violation.
     */
    public boolean handleOps() throws IOException, ProtocolException {
        synchronized (writer) {
            writer.write(sendBuffer);
            if (key.isWritable()) {
                sendBuffer.flip();
                channel.write(sendBuffer);
                sendBuffer.compact();
            }
        }

        while (key.isReadable()) {
            int count = channel.read(receiveBuffer);
            if (count == -1) {
                return true;
            }
            if (count == 0) {
                break;
            }
            receiveBuffer.flip();
            NameMessage message;
            while ((message = reader.read(receiveBuffer)) != null) {
                System.err.println("Request for client keys: " + message.name());
                scheduleKeyRetrieval(message.name());
            }
            receiveBuffer.compact();
        }

        synchronized (writer) {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            if (sendBuffer.position() != 0 || writer.hasData()) {
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            } else {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
        }

        return false;
    }

    /**
     * Gets the {@code SelectionKey} for this connection.
     *
     * @return The {@code SelectionKey}.
     */
    public SelectionKey key() {
        return key;
    }

    /**
     * Closes the connection.
     */
    @Override
    public void close() {
        key.cancel();
        try {
            channel.close();
        } catch (IOException exception) {
            System.err.println("Channel close failed: " + exception.getMessage());
        }
    }
}
