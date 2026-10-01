package ru.nsu.aeliseev2.taskj1.keyserver;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import ru.nsu.aeliseev2.taskj1.utils.KeyCertPair;

/**
 * An in-memory database/generator of cert/private key pairs for named clients.
 */
class KeyDatabase {
    private static class SavedKey {
        public KeyCertPair keyCertPair = null;
        public ArrayList<KeyCertCallback> callbacks = new ArrayList<>();
        public boolean generating = false;
    }

    private final ConcurrentHashMap<String, SavedKey> keyMap;
    private final Executor genExecutor;
    private final KeyPairGenerator keyGenerator;
    private final X500Name certIssuerName;
    private final SecureRandom random;
    private final ContentSigner certSigner;

    /**
     * Initializes a new instance of {@code KeyDatabase}.
     *
     * @param genThreadCount The number of threads to use to generate new key pairs.
     * @param issuerCN The common name of the X.509 certificate issuer.
     */
    public KeyDatabase(int genThreadCount, String issuerCN) {
        this(Executors.newFixedThreadPool(genThreadCount), issuerCN);
    }

    /**
     * Initializes a new instance of {@code KeyDatabase}.
     *
     * @param genExecutor The executor to use to generate new key pairs.
     * @param issuerCN The common name of the X.509 certificate issuer.
     */
    public KeyDatabase(Executor genExecutor, String issuerCN) {
        this.genExecutor = genExecutor;
        try {
            this.keyGenerator = KeyPairGenerator.getInstance("RSA");
            this.keyGenerator.initialize(8192);
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            throw new UnsupportedOperationException(
                "RSA 8192 is not supported by the current Java runtime.", e);
        }
        this.keyMap = new ConcurrentHashMap<>();
        this.certIssuerName = new X500NameBuilder().addRDN(BCStyle.CN, issuerCN).build();
        this.random = new SecureRandom();
        try {
            var issuerKeyPair = this.keyGenerator.generateKeyPair();
            this.certSigner = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(new BouncyCastleProvider())
                .build(issuerKeyPair.getPrivate());
        } catch (OperatorCreationException e) {
            throw new UnsupportedOperationException("Could not create SHA256withRSA signer.", e);
        }
    }

    /**
     * Schedules an asynchronous key pair retrieval/generation operation.
     *
     * @param name The name of the client to get the key pair for.
     * @param callback The callback to call when the operation completes.
     */
    public void getAsync(String name, KeyCertCallback callback) {
        var savedKey = keyMap.computeIfAbsent(name, n -> new SavedKey());
        synchronized (savedKey) {
            if (savedKey.keyCertPair == null) {
                if (!savedKey.generating) {
                    savedKey.generating = true;
                    genExecutor.execute(() -> {
                        var generatedKeyCertPair = generate(name);
                        synchronized (savedKey) {
                            savedKey.keyCertPair = generatedKeyCertPair;
                            savedKey.generating = false;
                        }
                        for (var cb : savedKey.callbacks) {
                            cb.keyCertGenerated(name, generatedKeyCertPair);
                        }
                        savedKey.callbacks = null;
                    });
                }
                savedKey.callbacks.add(callback);
                return;
            }
        }
        callback.keyCertGenerated(name, savedKey.keyCertPair);
    }

    private KeyCertPair generate(String subjectCN) {
        var keyPair = keyGenerator.generateKeyPair();
        var certSerial = new BigInteger(159, random);
        var dateFrom = Instant.now();
        var dateTo = dateFrom.plus(365, ChronoUnit.DAYS);
        var subjectName = new X500NameBuilder().addRDN(BCStyle.CN, subjectCN).build();
        var keyInfo = SubjectPublicKeyInfo.getInstance(
            ASN1Sequence.getInstance(keyPair.getPublic().getEncoded()));
        var builder = new X509v3CertificateBuilder(certIssuerName, certSerial,
            Date.from(dateFrom), Date.from(dateTo), subjectName, keyInfo);
        var certHolder = builder.build(certSigner);
        try {
            return new KeyCertPair(certHolder.getEncoded(), keyPair.getPrivate().getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
