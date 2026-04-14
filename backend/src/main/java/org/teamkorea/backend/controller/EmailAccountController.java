package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.dto.EmailAccountRequestDto;
import org.teamkorea.backend.service.EmailAccountService;

@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EmailAccountRequestDto dto) {

        Long id = emailAccountService.createEmailAccount(dto);

        return ResponseEntity.ok(id);
    }
}