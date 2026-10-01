package ru.nsu.aeliseev2.taskj1.keyserver;

import ru.nsu.aeliseev2.taskj1.utils.KeyCertPair;

/**
 * Callback for {@code KeyDatabase} to call after retrieving the keys.
 */
interface KeyCertCallback {
    /**
     * Called when the key retrieval/generation is complete.
     *
     * @param commonName The name of the client for whom the keys were generated.
     * @param keyCertPair The key/certificate pair for the client.
     */
    void keyCertGenerated(String commonName, KeyCertPair keyCertPair);
}
