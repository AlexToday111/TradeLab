package com.example.back.livetrading.service;

import com.example.back.livetrading.config.LiveTradingProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class LiveCredentialCryptoService {

    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public LiveCredentialCryptoService(LiveTradingProperties properties) {
        this.keySpec = new SecretKeySpec(sha256(properties.credentialEncryptionKey()), "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer payload = ByteBuffer.allocate(iv.length + encrypted.length);
            payload.put(iv);
            payload.put(encrypted);
            return Base64.getEncoder().encodeToString(payload.array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to encrypt live exchange credential", exception);
        }
    }

    public String decrypt(String payload) {
        try {
            byte[] decoded = Base64.getDecoder().decode(payload);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to decrypt live exchange credential", exception);
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
