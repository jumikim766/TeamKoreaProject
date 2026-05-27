package org.teamkorea.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.service.OpenAIService;

@RestController
@RequestMapping("/api/openai")
public class OpenAITestController {

    private final OpenAIService openAIService;

    public OpenAITestController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/test")
    public String test() {
        return openAIService.analyzeUrl("http://paypal-security-check.com/login");
    }
}