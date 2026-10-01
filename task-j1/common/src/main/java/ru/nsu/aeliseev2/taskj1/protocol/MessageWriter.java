package ru.nsu.aeliseev2.taskj1.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import ru.nsu.aeliseev2.taskj1.protocol.messages.Message;

/**
 * A stateful message writer.
 *
 * @see Message
 */
public class MessageWriter {
    private final ArrayDeque<Message> messages = new ArrayDeque<>();
    private Message.Serializer currentSerializer = null;

    /**
     * Adds a message to the writer's queue.
     *
     * @param message The message to enqueue.
     */
    public void enqueue(Message message) {
        System.err.println("Enqueued message: " + message);
        messages.add(message);
    }

    /**
     * Checks if the writer has data left to write.
     *
     * @return Whether the buffer has any data.
     */
    public boolean hasData() {
        return currentSerializer != null || !messages.isEmpty();
    }

    /**
     * Writes a chunk of message data (if any) to the buffer.
     *
     * @param buffer The buffer to write to.
     */
    public void write(ByteBuffer buffer) {
        while (hasData()) {
            if (currentSerializer != null) {
                int oldPosition = buffer.position();
                if (currentSerializer.write(buffer)) {
                    currentSerializer = null;
                }
                if (buffer.position() == oldPosition) {
                    // Serializer wasn't able to write any data.
                    break;
                }
            } else if (!messages.isEmpty()) {
                currentSerializer = messages.remove().serialize();
            }
        }
    }
}