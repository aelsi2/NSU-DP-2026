package ru.nsu.aeliseev2.taskj1.keyclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import ru.nsu.aeliseev2.taskj1.protocol.MessageReader;
import ru.nsu.aeliseev2.taskj1.protocol.ProtocolException;
import ru.nsu.aeliseev2.taskj1.protocol.messages.KeyCertMessage;
import ru.nsu.aeliseev2.taskj1.utils.KeyCertPair;

class ClientConnection {
    private static final int BUFFER_SIZE = 4096;

    private ClientConnection() {
    }

    public static KeyCertPair getKeyCertPair(InetSocketAddress address, String name,
                                             int delaySeconds, boolean error)
        throws IOException, ProtocolException {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer nameBuffer = ByteBuffer.allocate(nameBytes.length + 1);
        nameBuffer.put(nameBytes);
        nameBuffer.put((byte) 0);
        nameBuffer.flip();

        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(address);
            socketChannel.write(nameBuffer);

            try {
                Thread.sleep(delaySeconds * 1000L);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            if (error) {
                throw new IOException("Forced error.");
            }

            MessageReader<KeyCertMessage> reader =
                new MessageReader<>(new KeyCertMessage.Deserializer());
            ByteBuffer messageBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (true) {
                socketChannel.read(messageBuffer);
                messageBuffer.flip();
                KeyCertMessage message;
                if ((message = reader.read(messageBuffer)) != null) {
                    return message.keyCertPair();
                }
                messageBuffer.compact();
            }
        }
    }
}
