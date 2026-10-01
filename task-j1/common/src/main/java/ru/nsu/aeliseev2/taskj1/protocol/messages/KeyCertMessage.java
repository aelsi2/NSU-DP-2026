package ru.nsu.aeliseev2.taskj1.protocol.messages;

import java.nio.ByteBuffer;
import ru.nsu.aeliseev2.taskj1.utils.KeyCertPair;

/**
 * The message sent by the server to the client that contains the key/certificate pair
 */
public record KeyCertMessage(KeyCertPair keyCertPair) implements Message {
    private static final int HEADER_SIZE = 8;

    /**
     * Deserializer for {@code NameMessage}.
     */
    public static class Deserializer implements Message.Deserializer<KeyCertMessage> {
        private int certReadPos = 0;
        private int keyReadPos = 0;
        private byte[] cert = null;
        private byte[] privateKey = null;

        /**
         * {@inheritDoc}
         */
        @Override
        public KeyCertMessage read(ByteBuffer buffer) {
            if (cert == null || privateKey == null) {
                if (buffer.remaining() < HEADER_SIZE) {
                    return null;
                }
                cert = new byte[buffer.getInt()];
                privateKey = new byte[buffer.getInt()];
            }
            while (buffer.hasRemaining() && certReadPos < cert.length){
                int readCount = Integer.min(cert.length - certReadPos, buffer.remaining());
                buffer.get(cert, certReadPos, readCount);
                certReadPos += readCount;
            }
            if (certReadPos < cert.length) {
                return null;
            }
            while (buffer.hasRemaining() && keyReadPos < privateKey.length){
                int readCount = Integer.min(privateKey.length - keyReadPos, buffer.remaining());
                buffer.get(privateKey, keyReadPos, readCount);
                keyReadPos += readCount;
            }
            if (keyReadPos < privateKey.length) {
                return null;
            }
            return new KeyCertMessage(new KeyCertPair(cert, privateKey));
        }
    }

    private class Serializer implements Message.Serializer {
        private boolean headerWritten = false;
        private int certWritePos = 0;
        private int keyWritePos = 0;

        @Override
        public boolean write(ByteBuffer buffer) {
            if (!headerWritten) {
                if (buffer.remaining() < HEADER_SIZE) {
                    return false;
                }
                buffer.putInt(keyCertPair.cert().length);
                buffer.putInt(keyCertPair.privateKey().length);
                headerWritten = true;
            }
            var cert = keyCertPair.cert();
            var privateKey = keyCertPair.privateKey();
            while (buffer.hasRemaining() && certWritePos < cert.length) {
                int writeCount = Integer.min(cert.length - certWritePos, buffer.remaining());
                buffer.put(cert, certWritePos, writeCount);
                certWritePos += writeCount;
            }
            if (certWritePos < cert.length) {
                return false;
            }
            while (buffer.hasRemaining() && keyWritePos < privateKey.length) {
                int writeCount = Integer.min(privateKey.length - keyWritePos, buffer.remaining());
                buffer.put(privateKey, keyWritePos, writeCount);
                keyWritePos += writeCount;
            }
            if (keyWritePos < privateKey.length) {
                return false;
            }
            return true;
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
