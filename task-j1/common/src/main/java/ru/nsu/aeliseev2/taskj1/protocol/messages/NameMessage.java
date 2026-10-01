package ru.nsu.aeliseev2.taskj1.protocol.messages;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The message sent by the client to the server to identify itself.
 *
 * @param name The common name of the client.
 */
public record NameMessage(String name) implements Message {
    /**
     * Deserializer for {@code NameMessage}.
     */
    public static class Deserializer implements Message.Deserializer<NameMessage> {
        private final ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();

        /**
         * {@inheritDoc}
         */
        @Override
        public NameMessage read(ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                byte byt = buffer.get();
                if (byt != 0) {
                    arrayStream.write(byt);
                }
                else {
                    String name = arrayStream.toString(StandardCharsets.UTF_8);
                    arrayStream.reset();
                    return new NameMessage(name);
                }
            }
            return null;
        }
    }

    private class Serializer implements Message.Serializer {
        private final byte[] bytes;
        private int writePosition = 0;

        private Serializer() {
            byte[] asciiBytes = name.getBytes(StandardCharsets.UTF_8);
            bytes = new byte[asciiBytes.length + 1];
            System.arraycopy(asciiBytes, 0, bytes, 0, asciiBytes.length);
        }

        @Override
        public boolean write(ByteBuffer buffer) {
            while (buffer.hasRemaining() && writePosition < bytes.length) {
                int writeCount = Integer.min(buffer.remaining(), bytes.length - writePosition);
                buffer.put(bytes, writePosition, writeCount);
                writePosition += writeCount;
            }
            return writePosition == bytes.length;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message.Serializer serialize() {
        return new Serializer();
    }
}
