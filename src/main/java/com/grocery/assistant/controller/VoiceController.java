package com.grocery.assistant.controller;

import com.grocery.assistant.dto.VoicePriceParseRequest;
import com.grocery.assistant.dto.VoicePriceParseResult;
import com.grocery.assistant.service.VoiceCommandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceCommandService voiceCommandService;

    public VoiceController(VoiceCommandService voiceCommandService) {
        this.voiceCommandService = voiceCommandService;
    }

    @PostMapping("/parse-price-command")
    public ResponseEntity<VoicePriceParseResult> parsePriceCommand(@Valid @RequestBody VoicePriceParseRequest request) {
        VoicePriceParseResult result = voiceCommandService.parseVoicePriceCommand(request.getSpokenText());
        return ResponseEntity.ok(result);
    }
}
