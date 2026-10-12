package com.grocery.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public class VoicePriceParseRequest {

    @NotBlank(message = "Spoken text cannot be blank")
    private String spokenText;

    public VoicePriceParseRequest() {}

    public VoicePriceParseRequest(String spokenText) {
        this.spokenText = spokenText;
    }

    public String getSpokenText() {
        return spokenText;
    }

    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }
}
