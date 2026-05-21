package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;

    private static final SecureRandom secureRandom = new SecureRandom();

    // 영문 대문자/소문자/숫자 조합 인증번호 생성
    public String createVerificationCode() {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    // 인증번호 이메일 발송
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(toEmail);
            message.setSubject("[URL GUARD] 이메일 인증번호 안내");
            message.setText(
                    "안녕하세요. URL GUARD입니다.\n\n" +
                            "요청하신 인증번호는 아래와 같습니다.\n\n" +
                            "인증번호: " + code + "\n\n" +
                            "인증번호는 5분 동안만 유효합니다.\n" +
                            "본인이 요청하지 않았다면 이 메일을 무시해주세요.");

            mailSender.send(message);

        } catch (MailException e) {
            throw new RuntimeException("인증번호 이메일 발송에 실패했습니다.");
        }
    }
}