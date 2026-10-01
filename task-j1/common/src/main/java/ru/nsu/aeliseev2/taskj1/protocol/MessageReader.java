package ru.nsu.aeliseev2.taskj1.protocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import ru.nsu.aeliseev2.taskj1.protocol.messages.Message;

/**
 * A stateful message reader.
 *
 * @see Message
 */
public class MessageReader<T extends Message> {
    private final Message.Deserializer<T> deserializer;

    /**
     * Initializes a new instance of {@code MessageReader}.
     *
     * @param deserializer The message deserializer to use.
     */
    public MessageReader(Message.Deserializer<T> deserializer) {
        this.deserializer = deserializer;
    }

    /**
     * Reads a chunk of a message from the buffer.
     *
     * @param buffer The buffer to read from.
     * @return The message if all chunks of a message have been read, or {@code null}.
     * @throws ProtocolException The buffer has malformed data.
     */
    public T read(ByteBuffer buffer) throws ProtocolException {
        try {
            T message = deserializer.read(buffer);
            if (message != null) {
                System.err.println("Received message: " + message);
            }
            return message;
        } catch (Exception e) {
            throw new ProtocolException(e);
        }
    }
}
