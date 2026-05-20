package com.carrovivo.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${encryption.secret}")
    private String secret;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[16];
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar dado");
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[16];
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar dado");
        }
    }

    public String anonymize(String data) {
        if (data == null || data.length() < 4) return "***";
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }
}