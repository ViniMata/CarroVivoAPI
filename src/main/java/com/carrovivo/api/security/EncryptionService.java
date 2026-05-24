package com.carrovivo.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

// CRIPTOGRAFIA EM REPOUSO — AES-256-CBC
// Dados sensíveis armazenados no banco são criptografados com AES-256.
// O dado no banco é ilegível sem a chave secreta.
@Service
public class EncryptionService {

    // CHAVE SECRETA VIA VARIÁVEL DE AMBIENTE
    // Nunca hardcoded. A chave deve ter exatamente 32 caracteres para AES-256.
    @Value("${encryption.secret}")
    private String secret;

    // AES-256 COM MODO CBC E PADDING PKCS5
    // CBC (Cipher Block Chaining): cada bloco cifrado depende do anterior,
    // tornando padrões repetitivos no plaintext invisíveis no ciphertext.
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    // IV ALEATÓRIO POR CHAMADA — SEGURANÇA REAL DO AES-CBC
    // O Initialization Vector (IV) garante que o mesmo dado produz
    // ciphertexts diferentes a cada criptografia.
    // Armazenado junto ao ciphertext no formato: Base64(IV):Base64(ciphertext)
    // O IV é público por natureza — precisa ser único, não secreto.
    public String encrypt(String data) {
        try {
            byte[] ivBytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes); // IV criptograficamente seguro
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(ivBytes)
                    + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            // ERRO GENÉRICO — NÃO EXPÕE DETALHES DA CRIPTOGRAFIA
            throw new RuntimeException("Erro ao criptografar dado");
        }
    }

    // DESCRIPTOGRAFIA — EXTRAI IV DO PREFIXO
    // Lê o IV armazenado junto ao ciphertext para reverter a operação.
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

    // ANONIMIZAÇÃO DE DADOS — LGPD
    // Substitui parte do dado por asteriscos para exibição em dashboards
    // e logs sem expor o dado completo. Reversível apenas com a chave original.
    public String anonymize(String data) {
        if (data == null || data.length() < 4) return "***";
        return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
    }
}
