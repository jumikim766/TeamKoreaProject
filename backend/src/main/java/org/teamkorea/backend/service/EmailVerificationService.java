package org.teamkorea.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamkorea.backend.domain.EmailVerificationCode;
import org.teamkorea.backend.exception.BusinessException;
import org.teamkorea.backend.exception.ErrorCode;
import org.teamkorea.backend.repository.EmailVerificationCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    // 영문 대문자/소문자/숫자 조합 인증번호 생성
    public String createVerificationCode() {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = secureRandom.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    // 공통 인증번호 발송: 생성 + 저장 + 이메일 발송
    @Transactional
    public void sendCode(String email, String purpose) {
        String code = createVerificationCode();

        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .build();

        emailVerificationCodeRepository.save(verificationCode);

        sendVerificationCode(email, code);
    }

    // 공통 인증번호 검증: 만료/불일치 체크 후 verified=true 처리
    @Transactional
    public void verifyCode(String email, String code, String purpose) {
        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_VERIFICATION_CODE));

        if (verificationCode.isVerified()) {
            throw new BusinessException(
                    ErrorCode.ALREADY_VERIFIED_CODE);
        }

        if (verificationCode.isExpired()) {
            throw new BusinessException(
                    ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (code == null || code.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (!verificationCode.getCode().equals(code)) {
            throw new BusinessException(
                    ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verificationCode.markVerified();
    }

    // PATCH 전에 이메일 변경 인증이 완료됐는지 확인
    @Transactional(readOnly = true)
    public void validateVerifiedCode(String email, String purpose) {
        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_VERIFICATION_CODE));

        if (!verificationCode.isVerified()) {
            throw new BusinessException(
                    ErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (verificationCode.isExpired()) {
            throw new BusinessException(
                    ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
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
                            "인증번호는 3분 동안만 유효합니다.\n" +
                            "본인이 요청하지 않았다면 이 메일을 무시해주세요.");

            mailSender.send(message);

        } catch (MailException e) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "인증번호 이메일 발송에 실패했습니다.");
        }
    }
}