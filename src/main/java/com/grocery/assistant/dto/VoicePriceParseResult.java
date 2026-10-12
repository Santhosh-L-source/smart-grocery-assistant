package com.grocery.assistant.dto;

import com.grocery.assistant.entity.enums.Unit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VoicePriceParseResult {

    private String recognizedText;
    private boolean matched;
    private boolean ambiguous;
    private Long productId;
    private String productNameEn;
    private String productNameTa;
    private Unit unit;
    private BigDecimal currentSellingPrice;
    private BigDecimal proposedSellingPrice;
    private String spokenConfirmationTamil;
    private List<ProductDto> matchingCandidates = new ArrayList<>();
    private String errorMessage;

    public VoicePriceParseResult() {}

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public void setAmbiguous(boolean ambiguous) {
        this.ambiguous = ambiguous;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductNameEn() {
        return productNameEn;
    }

    public void setProductNameEn(String productNameEn) {
        this.productNameEn = productNameEn;
    }

    public String getProductNameTa() {
        return productNameTa;
    }

    public void setProductNameTa(String productNameTa) {
        this.productNameTa = productNameTa;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BigDecimal getCurrentSellingPrice() {
        return currentSellingPrice;
    }

    public void setCurrentSellingPrice(BigDecimal currentSellingPrice) {
        this.currentSellingPrice = currentSellingPrice;
    }

    public BigDecimal getProposedSellingPrice() {
        return proposedSellingPrice;
    }

    public void setProposedSellingPrice(BigDecimal proposedSellingPrice) {
        this.proposedSellingPrice = proposedSellingPrice;
    }

    public String getSpokenConfirmationTamil() {
        return spokenConfirmationTamil;
    }

    public void setSpokenConfirmationTamil(String spokenConfirmationTamil) {
        this.spokenConfirmationTamil = spokenConfirmationTamil;
    }

    public List<ProductDto> getMatchingCandidates() {
        return matchingCandidates;
    }

    public void setMatchingCandidates(List<ProductDto> matchingCandidates) {
        this.matchingCandidates = matchingCandidates;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
