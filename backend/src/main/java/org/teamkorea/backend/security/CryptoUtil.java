package org.teamkorea.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Component
public class CryptoUtil {

    private static final String ALGORITHM = "AES";

    private final SecretKeySpec secretKeySpec;

    // ===== 추가: application.properties의 app.crypto.secret-key 값을 주입받음 =====
    public CryptoUtil(@Value("${app.crypto.secret-key}") String secretKey) {
        if (secretKey == null ||
                !(secretKey.length() == 16 || secretKey.length() == 24 || secretKey.length() == 32)) {
            throw new IllegalArgumentException("AES secret-key는 16자, 24자, 32자 중 하나여야 합니다.");
        }

        this.secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
    }

    // ===== 추가: 평문 문자열을 AES 암호화해서 byte[]로 반환 =====
    public byte[] encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isBlank()) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            return cipher.doFinal(plainText.getBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("암호화 처리 중 오류가 발생했습니다.");
        }
    }

    // ===== 추가: byte[] 암호문을 복호화해서 평문 문자열로 반환 =====
    public String decrypt(byte[] encryptedData) {
        try {
            if (encryptedData == null || encryptedData.length == 0) {
                return null;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);

            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("복호화 처리 중 오류가 발생했습니다.");
        }
    }
}