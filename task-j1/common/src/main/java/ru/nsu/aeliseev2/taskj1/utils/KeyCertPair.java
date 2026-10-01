package ru.nsu.aeliseev2.taskj1.utils;

/**
 * A pair of a private key and a certificate for a client.
 *
 * @param cert The DER-encoded certificate.
 * @param privateKey The DER-encoded private key.
 */
public record KeyCertPair(byte[] cert, byte[] privateKey) {
}
