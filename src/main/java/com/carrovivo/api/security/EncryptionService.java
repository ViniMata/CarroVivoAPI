package com.carrovivo.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${encryption.secret}")
    private String secret;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    public String encrypt(String data) {
        try {
            byte[] ivBytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(ivBytes)
                    + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado");
        }
    }

    public String decrypt(String encryptedData) {
        try {
            String[] parts = encryptedData.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Formato inválido");

            byte[] ivBytes = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);

            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            return new String(cipher.doFinal(cipherBytes));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado");
        }
    }

    public String anonymize(String data) {
        if (data == null || data.length() < 4) return "***";
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }
}
