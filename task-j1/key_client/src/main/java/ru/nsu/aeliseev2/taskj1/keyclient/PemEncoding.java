package ru.nsu.aeliseev2.taskj1.keyclient;

import java.util.Base64;

class PemEncoding {
    private PemEncoding() {
    }

    public static String convert(byte[] derBytes, String pemType) {
        Base64.Encoder mimeEncoder = Base64.getMimeEncoder(64, new byte[]{'\n'});
        String base64Encoded = mimeEncoder.encodeToString(derBytes);

        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN ").append(pemType).append("-----\n");
        builder.append(base64Encoded).append("\n");
        builder.append("-----END ").append(pemType).append("-----\n");
        return builder.toString();
    }
}
