package com.grocery.assistant.entity.enums;

public enum Unit {
    PIECE("Piece", "பீஸ்", false),
    PACKET("Packet", "பாக்கெட்", false),
    BOTTLE("Bottle", "பாட்டில்", false),
    KG("Kilogram", "கிலோ", true),
    GRAM("Gram", "கிராம்", true),
    LITRE("Litre", "லிட்டர்", true),
    ML("Millilitre", "மில்லி", true);

    private final String displayNameEn;
    private final String displayNameTa;
    private final boolean loose;

    Unit(String displayNameEn, String displayNameTa, boolean loose) {
        this.displayNameEn = displayNameEn;
        this.displayNameTa = displayNameTa;
        this.loose = loose;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public String getDisplayNameTa() {
        return displayNameTa;
    }

    public boolean isLoose() {
        return loose;
    }
}
