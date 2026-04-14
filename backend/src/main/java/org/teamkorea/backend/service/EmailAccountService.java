package org.teamkorea.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teamkorea.backend.domain.EmailAccount;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.repository.EmailAccountRepository;
import org.teamkorea.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;

    public Long createEmailAccount(EmailAccountRequestDto dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailAccount account = EmailAccount.builder()
                .user(user)
                .email(dto.getEmail())
                .provider(dto.getProvider())
                .imapHost(dto.getImapHost())
                .imapPort(dto.getImapPort())
                .loginId(dto.getLoginId())
                .secretEnc(dto.getPassword().getBytes()) // 임시
                .active(true)
                .build();

        EmailAccount saved = emailAccountRepository.save(account);

        return saved.getAccountId();
    }
}