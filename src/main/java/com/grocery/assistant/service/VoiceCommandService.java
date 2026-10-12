package com.grocery.assistant.service;

import com.grocery.assistant.dto.ProductDto;
import com.grocery.assistant.dto.VoicePriceParseResult;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VoiceCommandService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final TamilVoiceService tamilVoiceService;

    private static final Map<String, Integer> TAMIL_WORD_NUMBERS = new HashMap<>();
    private static final Map<String, Integer> ENGLISH_WORD_NUMBERS = new HashMap<>();

    static {
        // Tamil words for numbers
        TAMIL_WORD_NUMBERS.put("பத்து", 10);
        TAMIL_WORD_NUMBERS.put("இருபது", 20);
        TAMIL_WORD_NUMBERS.put("முப்பது", 30);
        TAMIL_WORD_NUMBERS.put("முப்பத்தைந்து", 35);
        TAMIL_WORD_NUMBERS.put("நாற்பது", 40);
        TAMIL_WORD_NUMBERS.put("நாற்பத்தெட்டு", 48);
        TAMIL_WORD_NUMBERS.put("ஐம்பது", 50);
        TAMIL_WORD_NUMBERS.put("ஐம்பத்தைந்து", 55);
        TAMIL_WORD_NUMBERS.put("அறுபது", 60);
        TAMIL_WORD_NUMBERS.put("எழுபது", 70);
        TAMIL_WORD_NUMBERS.put("எழுபத்தைந்து", 75);
        TAMIL_WORD_NUMBERS.put("எண்பது", 80);
        TAMIL_WORD_NUMBERS.put("தொண்ணூறு", 90);
        TAMIL_WORD_NUMBERS.put("நூறு", 100);
        TAMIL_WORD_NUMBERS.put("நூற்று பத்து", 110);
        TAMIL_WORD_NUMBERS.put("நூற்று இருபது", 120);
        TAMIL_WORD_NUMBERS.put("நூற்று இருபத்தைந்து", 125);
        TAMIL_WORD_NUMBERS.put("நூற்று நாற்பது", 140);

        // English words for numbers
        ENGLISH_WORD_NUMBERS.put("ten", 10);
        ENGLISH_WORD_NUMBERS.put("twenty", 20);
        ENGLISH_WORD_NUMBERS.put("thirty", 30);
        ENGLISH_WORD_NUMBERS.put("thirty five", 35);
        ENGLISH_WORD_NUMBERS.put("forty", 40);
        ENGLISH_WORD_NUMBERS.put("forty eight", 48);
        ENGLISH_WORD_NUMBERS.put("fifty", 50);
        ENGLISH_WORD_NUMBERS.put("fifty five", 55);
        ENGLISH_WORD_NUMBERS.put("sixty", 60);
        ENGLISH_WORD_NUMBERS.put("seventy", 70);
        ENGLISH_WORD_NUMBERS.put("seventy five", 75);
        ENGLISH_WORD_NUMBERS.put("eighty", 80);
        ENGLISH_WORD_NUMBERS.put("ninety", 90);
        ENGLISH_WORD_NUMBERS.put("hundred", 100);
        ENGLISH_WORD_NUMBERS.put("one hundred twenty five", 125);
    }

    public VoiceCommandService(ProductRepository productRepository,
                               ProductService productService,
                               TamilVoiceService tamilVoiceService) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.tamilVoiceService = tamilVoiceService;
    }

    /**
     * Parses a voice command transcript into product identification and proposed price.
     * Guaranteed never to update any database record.
     */
    public VoicePriceParseResult parseVoicePriceCommand(String rawTranscript) {
        VoicePriceParseResult result = new VoicePriceParseResult();
        result.setRecognizedText(rawTranscript != null ? rawTranscript.trim() : "");

        if (rawTranscript == null || rawTranscript.trim().isEmpty()) {
            result.setMatched(false);
            result.setErrorMessage("No voice command detected. Please speak clearly.");
            return result;
        }

        String text = rawTranscript.trim().toLowerCase();

        // 1. Extract proposed price
        BigDecimal proposedPrice = extractPrice(text);
        if (proposedPrice == null) {
            result.setMatched(false);
            result.setErrorMessage("Could not identify the new price from voice command: '" + rawTranscript + "'. Please specify rupees.");
            return result;
        }
        result.setProposedSellingPrice(proposedPrice);

        // 2. Extract unit if present
        Unit spokenUnit = extractUnit(text);
        result.setUnit(spokenUnit);

        // 3. Extract candidate product keywords
        String productQuery = extractProductKeyword(text);
        if (productQuery.isEmpty()) {
            result.setMatched(false);
            result.setErrorMessage("Could not identify product name in voice command: '" + rawTranscript + "'.");
            return result;
        }

        // 4. Search database for matching active products
        List<Product> matches = productRepository.searchActiveProducts(productQuery);

        if (matches.isEmpty()) {
            result.setMatched(false);
            result.setErrorMessage("No products found matching '" + productQuery + "'. Please verify product name.");
            return result;
        }

        if (matches.size() > 1) {
            // Ambiguity detected
            result.setMatched(false);
            result.setAmbiguous(true);
            result.setErrorMessage("Multiple products found for '" + productQuery + "'. Please select the intended product.");
            List<ProductDto> candidates = matches.stream()
                    .map(productService::toDto)
                    .collect(Collectors.toList());
            result.setMatchingCandidates(candidates);
            return result;
        }

        // Exact single product match found
        Product matchedProduct = matches.get(0);
        result.setMatched(true);
        result.setAmbiguous(false);
        result.setProductId(matchedProduct.getId());
        result.setProductNameEn(matchedProduct.getNameEn());
        result.setProductNameTa(matchedProduct.getNameTa());
        result.setUnit(spokenUnit != null ? spokenUnit : matchedProduct.getUnit());
        result.setCurrentSellingPrice(matchedProduct.getSellingPrice());

        // Prepare spoken Tamil confirmation prompt
        String confirmTamil = tamilVoiceService.formatVoiceConfirmationPrompt(
                matchedProduct.getNameTa(), matchedProduct.getNameEn(), proposedPrice
        );
        result.setSpokenConfirmationTamil(confirmTamil);

        return result;
    }

    private BigDecimal extractPrice(String text) {
        // Direct numeric match: e.g. "55", "55.50", "₹55", "55 rupees"
        Pattern numPattern = Pattern.compile("(?:to|at|is|rate|price|விலை|ரூபாய்|rs\\.?|inr)?\\s*(?:₹|rs\\.?\\s*)?(\\d+(?:\\.\\d+)?)\\s*(?:rupees|rs|ரூபாய்)?");
        Matcher m = numPattern.matcher(text);
        BigDecimal foundPrice = null;

        while (m.find()) {
            try {
                String val = m.group(1);
                BigDecimal parsed = new BigDecimal(val);
                if (parsed.compareTo(BigDecimal.ZERO) > 0) {
                    foundPrice = parsed;
                }
            } catch (Exception ignored) {}
        }

        if (foundPrice != null) {
            return foundPrice;
        }

        // Check word numbers (Tamil)
        for (Map.Entry<String, Integer> entry : TAMIL_WORD_NUMBERS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return new BigDecimal(entry.getValue());
            }
        }

        // Check word numbers (English)
        for (Map.Entry<String, Integer> entry : ENGLISH_WORD_NUMBERS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return new BigDecimal(entry.getValue());
            }
        }

        return null;
    }

    private Unit extractUnit(String text) {
        if (text.contains("kilogram") || text.contains(" kg") || text.contains("கிலோ")) {
            return Unit.KG;
        }
        if (text.contains("gram") || text.contains(" கிராம்")) {
            return Unit.GRAM;
        }
        if (text.contains("litre") || text.contains("liter") || text.contains("லிட்டர்")) {
            return Unit.LITRE;
        }
        if (text.contains("ml") || text.contains("மில்லி")) {
            return Unit.ML;
        }
        if (text.contains("piece") || text.contains("பீஸ்")) {
            return Unit.PIECE;
        }
        if (text.contains("packet") || text.contains("பாக்கெட்")) {
            return Unit.PACKET;
        }
        if (text.contains("bottle") || text.contains("பாட்டில்")) {
            return Unit.BOTTLE;
        }
        return null;
    }

    private String extractProductKeyword(String text) {
        // Strip common command carrier words
        String cleaned = text
                .replaceAll("(change|update|set|price|of|to|rupees|rupee|per|inr|rs|for)", " ")
                .replaceAll("(விலை|மாற்று|ரூபாய்|ஆக|என்று|ஒரு)", " ")
                .replaceAll("(kilogram|kg|gram|litre|liter|ml|piece|packet|bottle)", " ")
                .replaceAll("(கிலோ|கிராம்|லிட்டர்|மில்லி|பீஸ்|பாக்கெட்|பாட்டில்)", " ")
                .replaceAll("[0-9\\.\\,\\?]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return cleaned;
    }
}
