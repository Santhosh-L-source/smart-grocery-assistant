package com.grocery.assistant.service;

import com.grocery.assistant.entity.enums.Unit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class TamilVoiceService {

    private static final Map<Integer, String> ONES = new HashMap<>();
    private static final Map<Integer, String> TEENS = new HashMap<>();
    private static final Map<Integer, String> TENS_PREFIX = new HashMap<>();
    private static final Map<Integer, String> TENS_EXACT = new HashMap<>();

    static {
        ONES.put(0, "பூஜ்ஜியம்");
        ONES.put(1, "ஒன்று");
        ONES.put(2, "இரண்டு");
        ONES.put(3, "மூன்று");
        ONES.put(4, "நான்கு");
        ONES.put(5, "ஐந்து");
        ONES.put(6, "ஆறு");
        ONES.put(7, "ஏழு");
        ONES.put(8, "எட்டு");
        ONES.put(9, "ஒன்பது");

        TEENS.put(10, "பத்து");
        TEENS.put(11, "பதினொன்று");
        TEENS.put(12, "பன்னிரண்டு");
        TEENS.put(13, "பதின்மூன்று");
        TEENS.put(14, "பதினான்கு");
        TEENS.put(15, "பதினைந்து");
        TEENS.put(16, "பதினாறு");
        TEENS.put(17, "பதினேழு");
        TEENS.put(18, "பதினெட்டு");
        TEENS.put(19, "பத்தொன்பது");

        TENS_EXACT.put(20, "இருபது");
        TENS_EXACT.put(30, "முப்பது");
        TENS_EXACT.put(40, "நாற்பது");
        TENS_EXACT.put(50, "ஐம்பது");
        TENS_EXACT.put(60, "அறுபது");
        TENS_EXACT.put(70, "எழுபது");
        TENS_EXACT.put(80, "எண்பது");
        TENS_EXACT.put(90, "தொண்ணூறு");

        TENS_PREFIX.put(20, "இருபத்து ");
        TENS_PREFIX.put(30, "முப்பத்து ");
        TENS_PREFIX.put(40, "நாற்பத்து ");
        TENS_PREFIX.put(50, "ஐம்பத்து ");
        TENS_PREFIX.put(60, "அறுபத்து ");
        TENS_PREFIX.put(70, "எழுபத்து ");
        TENS_PREFIX.put(80, "எண்பத்து ");
        TENS_PREFIX.put(90, "தொண்ணூற்று ");
    }

    /**
     * Converts an integer up to 99,999 to Tamil words.
     */
    public String convertNumberToTamil(long number) {
        if (number == 0) {
            return "பூஜ்ஜியம்";
        }
        if (number < 0) {
            return "மைனஸ் " + convertNumberToTamil(Math.abs(number));
        }

        StringBuilder sb = new StringBuilder();

        // Thousands (ஆயிரம்)
        if (number >= 1000) {
            long thousands = number / 1000;
            number %= 1000;
            if (thousands == 1) {
                sb.append("ஆயிரத்து ");
            } else {
                sb.append(convertUnderThousand(thousands)).append(" ஆயிரம் ");
            }
        }

        // Hundreds (நூறு)
        if (number >= 100) {
            long hundreds = number / 100;
            number %= 100;
            switch ((int) hundreds) {
                case 1 -> sb.append(number == 0 ? "நூறு" : "நூற்று ");
                case 2 -> sb.append(number == 0 ? "இருநூறு" : "இருநூற்று ");
                case 3 -> sb.append(number == 0 ? "முந்நூறு" : "முந்நூற்று ");
                case 4 -> sb.append(number == 0 ? "நானூறு" : "நானூற்று ");
                case 5 -> sb.append(number == 0 ? "ஐந்நூறு" : "ஐந்நூற்று ");
                case 6 -> sb.append(number == 0 ? "அறுநூறு" : "அறுநூற்று ");
                case 7 -> sb.append(number == 0 ? "எழுநூறு" : "எழுநூற்று ");
                case 8 -> sb.append(number == 0 ? "எண்ணூறு" : "எண்ணூற்று ");
                case 9 -> sb.append(number == 0 ? "தொள்ளாயிரம்" : "தொள்ளாயிரத்து ");
            }
        }

        // 1 to 99
        if (number > 0) {
            sb.append(convertUnderHundred((int) number));
        }

        return sb.toString().trim();
    }

    private String convertUnderThousand(long n) {
        if (n < 100) {
            return convertUnderHundred((int) n);
        }
        long hundreds = n / 100;
        long rem = n % 100;
        String hStr = switch ((int) hundreds) {
            case 1 -> rem == 0 ? "நூறு" : "நூற்று ";
            case 2 -> rem == 0 ? "இருநூறு" : "இருநூற்று ";
            case 3 -> rem == 0 ? "முந்நூறு" : "முந்நூற்று ";
            case 4 -> rem == 0 ? "நானூறு" : "நானூற்று ";
            case 5 -> rem == 0 ? "ஐந்நூறு" : "ஐந்நூற்று ";
            case 6 -> rem == 0 ? "அறுநூறு" : "அறுநூற்று ";
            case 7 -> rem == 0 ? "எழுநூறு" : "எழுநூற்று ";
            case 8 -> rem == 0 ? "எண்ணூறு" : "எண்ணூற்று ";
            default -> rem == 0 ? "தொள்ளாயிரம்" : "தொள்ளாயிரத்து ";
        };
        return rem == 0 ? hStr : hStr + convertUnderHundred((int) rem);
    }

    private String convertUnderHundred(int n) {
        if (n < 10) {
            return ONES.get(n);
        }
        if (n < 20) {
            return TEENS.get(n);
        }
        int tens = (n / 10) * 10;
        int rem = n % 10;
        if (rem == 0) {
            return TENS_EXACT.get(tens);
        }
        return combineTensAndUnit(tens, rem);
    }

    private String combineTensAndUnit(int tens, int rem) {
        String base = switch (tens) {
            case 20 -> "இருபத்";
            case 30 -> "முப்பத்";
            case 40 -> "நாற்பத்";
            case 50 -> "ஐம்பத்";
            case 60 -> "அறுபத்";
            case 70 -> "எழுபத்";
            case 80 -> "எண்பத்";
            case 90 -> "தொண்ணூற்ற";
            default -> "";
        };

        if (tens == 90) {
            return switch (rem) {
                case 1 -> "தொண்ணூற்றொன்று";
                case 2 -> "தொண்ணூற்றிரண்டு";
                case 3 -> "தொண்ணூற்றுமூன்று";
                case 4 -> "தொண்ணூற்றுநான்கு";
                case 5 -> "தொண்ணூற்றைந்து";
                case 6 -> "தொண்ணூற்றாறு";
                case 7 -> "தொண்ணூற்றேழு";
                case 8 -> "தொண்ணூற்றெட்டு";
                case 9 -> "தொண்ணூற்றொன்பது";
                default -> "தொண்ணூறு";
            };
        }

        return switch (rem) {
            case 1 -> base + "தொன்று";
            case 2 -> base + "திரண்டு";
            case 3 -> base + "துமூன்று";
            case 4 -> base + "துநான்கு";
            case 5 -> base + "தைந்து";
            case 6 -> base + "தாறு";
            case 7 -> base + "தேழு";
            case 8 -> base + "தெட்டு";
            case 9 -> base + "தொன்பது";
            default -> TENS_EXACT.get(tens);
        };
    }

    /**
     * Converts a monetary amount to Tamil words (handles whole rupees and paise).
     */
    public String convertCurrencyToTamil(BigDecimal amount) {
        if (amount == null) {
            return "பூஜ்ஜியம் ரூபாய்";
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        long rupees = scaled.longValue();
        int paise = scaled.remainder(BigDecimal.ONE).multiply(new BigDecimal("100")).intValue();

        String rupeeWords = convertNumberToTamil(rupees);
        if (paise == 0) {
            return rupeeWords + " ரூபாய்";
        }
        return rupeeWords + " ரூபாய் " + convertNumberToTamil(paise) + " காசுகள்";
    }

    /**
     * Builds natural Tamil announcement for a product price lookup.
     * Examples:
     * "ஒரு கிலோ அரிசி ஐம்பத்தைந்து ரூபாய்."
     * "ஒரு சோப்பின் விலை முப்பத்தைந்து ரூபாய்."
     */
    public String formatProductPriceAnnouncement(String nameTa, String nameEn, Unit unit, BigDecimal price) {
        String prodName = (nameTa != null && !nameTa.isBlank()) ? nameTa : nameEn;
        String priceText = convertCurrencyToTamil(price);

        if (unit == Unit.PIECE) {
            String suffix = prodName.endsWith("ப்") ? "பின் விலை " : (prodName.endsWith("்") ? "ின் விலை " : " விலை ");
            return "ஒரு " + prodName + suffix + priceText + ".";
        }

        String unitPrefix = switch (unit) {
            case KG -> "ஒரு கிலோ ";
            case GRAM -> "ஒரு கிராம் ";
            case LITRE -> "ஒரு லிட்டர் ";
            case ML -> "ஒரு மில்லி ";
            case PACKET -> "ஒரு பாக்கெட் ";
            case BOTTLE -> "ஒரு பாட்டில் ";
            default -> "ஒரு ";
        };

        return unitPrefix + prodName + " " + priceText + ".";
    }

    /**
     * Builds Tamil announcement for a final or running bill total.
     * Example: "மொத்த தொகை நூற்று எண்பத்தைந்து ரூபாய்."
     */
    public String formatBillTotalAnnouncement(BigDecimal total) {
        return "மொத்த தொகை " + convertCurrencyToTamil(total) + ".";
    }

    /**
     * Builds Tamil confirmation question for voice price updates.
     * Example: "அரிசி விலை ஐம்பத்தைந்து ரூபாய் என்று மாற்றவா?"
     */
    public String formatVoiceConfirmationPrompt(String nameTa, String nameEn, BigDecimal proposedPrice) {
        String prodName = (nameTa != null && !nameTa.isBlank()) ? nameTa : nameEn;
        String priceWords = convertCurrencyToTamil(proposedPrice);
        return prodName + " விலை " + priceWords + " என்று மாற்றவா?";
    }
}
