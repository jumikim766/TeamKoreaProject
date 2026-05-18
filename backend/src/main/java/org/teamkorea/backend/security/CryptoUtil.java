package org.teamkorea.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Component
public class CryptoUtil {

    private static final String ALGORITHM = "AES"; // AES 알고리즘 사용
    private static final String TRANSFORMATION = "AES/GCM/NoPadding"; // AES-GCM 방식 사용

    private static final int KEY_VERSION = 1; // 암호화 키 버전 (추후 키 교체 대비)
    private static final int IV_LENGTH = 12; // GCM 권장 IV 길이 = 12byte
    private static final int TAG_LENGTH_BIT = 128; // 인증 태그 길이 = 128bit

    private final SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom(); // 매 암호화마다 랜덤 IV 생성용

    // application.properties 의 AES 키 주입
    public CryptoUtil(@Value("${app.crypto.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // AES 키 길이 검증
        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("AES secret-key는 16바이트, 24바이트, 32바이트 중 하나여야 합니다.");
        }

        this.secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }
    // 평문 문자열 AES-GCM 암호화
    public byte[] encrypt(String plainText) {
        try {
            // null 또는 공백이면 저장 안 함
            if (plainText == null || plainText.isBlank()) {
                return null;
            }
            // 12byte 랜덤 IV 생성
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // GCM 파라미터 설정
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKeySpec,
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            // 암호화 수행
            byte[] cipherTextWithTag = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 저장 구조:
            // [KEY_VERSION][IV][CIPHERTEXT + TAG]
            ByteBuffer byteBuffer = ByteBuffer.allocate(1 + IV_LENGTH + cipherTextWithTag.length);
            byteBuffer.put((byte) KEY_VERSION);
            byteBuffer.put(iv);
            byteBuffer.put(cipherTextWithTag);

            return byteBuffer.array();

        } catch (Exception e) {
            throw new IllegalArgumentException("암호화 처리 중 오류가 발생했습니다.", e);
        }
    }
    // AES-GCM 복호화
    public String decrypt(byte[] encryptedData) {
        try {
            // 데이터 없으면 null 반환
            if (encryptedData == null || encryptedData.length == 0) {
                return null;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            // KEY_VERSION 읽기
            int keyVersion = byteBuffer.get();
            // 현재 지원하는 버전인지 검증
            if (keyVersion != KEY_VERSION) {
                throw new IllegalArgumentException("지원하지 않는 암호화 키 버전입니다.");
            }
            // IV 추출
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            // 실제 암호문 + 인증태그 추출
            byte[] cipherTextWithTag = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherTextWithTag);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // 복호화 모드 초기화
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKeySpec,
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            // 복호화 수행
            byte[] plainTextBytes = cipher.doFinal(cipherTextWithTag);

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalArgumentException("복호화 처리 중 오류가 발생했습니다.", e);
        }
    }
}