package com.grocery.assistant.config;

import com.grocery.assistant.entity.PriceHistory;
import com.grocery.assistant.entity.Product;
import com.grocery.assistant.entity.User;
import com.grocery.assistant.entity.enums.ProductType;
import com.grocery.assistant.entity.enums.Role;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.entity.enums.UpdateMethod;
import com.grocery.assistant.repository.PriceHistoryRepository;
import com.grocery.assistant.repository.ProductRepository;
import com.grocery.assistant.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           PriceHistoryRepository priceHistoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersIfEmpty();
        seedProductsIfEmpty();
    }

    private void seedUsersIfEmpty() {
        if (userRepository.count() == 0) {
            log.info("Pre-seeding default application users...");
            User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN, "Store Owner / Admin");
            User shopkeeper = new User("shopkeeper", passwordEncoder.encode("shop123"), Role.SHOPKEEPER, "Shopkeeper Counter");
            userRepository.saveAll(List.of(admin, shopkeeper));
            log.info("Users seeded: admin (admin123), shopkeeper (shop123)");
        }
    }

    private void seedProductsIfEmpty() {
        if (productRepository.count() == 0) {
            log.info("Pre-seeding sample grocery products...");

            Product p1 = create("Rice (Ponni)", "பொன்னி அரிசி", "8901030000101", "Grains & Pulses", ProductType.LOOSE, Unit.KG, new BigDecimal("45.00"), new BigDecimal("55.00"), new BigDecimal("150.000"), new BigDecimal("20.000"));
            Product p2 = create("Sugar", "சர்க்கரை", "8901030000102", "Essentials", ProductType.LOOSE, Unit.KG, new BigDecimal("40.00"), new BigDecimal("48.00"), new BigDecimal("95.500"), new BigDecimal("15.000"));
            Product p3 = create("Bath Soap (Lifebuoy)", "லைஃப்பாய் சோப்", "8901030000103", "Personal Care", ProductType.PACKAGED, Unit.PIECE, new BigDecimal("28.00"), new BigDecimal("35.00"), new BigDecimal("48.000"), new BigDecimal("10.000"));
            Product p4 = create("Marie Gold Biscuits", "மேரி கோல்ட் பிஸ்கட்", "8901030000104", "Snacks", ProductType.PACKAGED, Unit.PACKET, new BigDecimal("15.00"), new BigDecimal("20.00"), new BigDecimal("60.000"), new BigDecimal("12.000"));
            Product p5 = create("Refined Sunflower Oil", "சூரியகாந்தி எண்ணெய்", "8901030000105", "Oils", ProductType.LOOSE, Unit.LITRE, new BigDecimal("110.00"), new BigDecimal("125.00"), new BigDecimal("45.000"), new BigDecimal("10.000"));
            Product p6 = create("Toor Dal", "துவரம் பருப்பு", "8901030000106", "Grains & Pulses", ProductType.LOOSE, Unit.KG, new BigDecimal("120.00"), new BigDecimal("140.00"), new BigDecimal("50.000"), new BigDecimal("10.000"));
            Product p7 = create("Chakra Gold Tea", "சக்ரா கோல்ட் டீ", "8901030000107", "Beverages", ProductType.PACKAGED, Unit.PACKET, new BigDecimal("60.00"), new BigDecimal("75.00"), new BigDecimal("30.000"), new BigDecimal("5.000"));
            Product p8 = create("Aavin Milk Packet (500ml)", "ஆவின் பால் பாக்கெட்", "8901030000108", "Dairy", ProductType.PACKAGED, Unit.PACKET, new BigDecimal("26.00"), new BigDecimal("30.00"), new BigDecimal("40.000"), new BigDecimal("10.000"));
            Product p9 = create("Tata Crystal Salt (1kg)", "டாடா உப்பு", "8901030000109", "Essentials", ProductType.PACKAGED, Unit.PACKET, new BigDecimal("10.00"), new BigDecimal("15.00"), new BigDecimal("50.000"), new BigDecimal("15.000"));
            Product p10 = create("Onion", "வெங்காயம்", "8901030000110", "Vegetables", ProductType.LOOSE, Unit.KG, new BigDecimal("30.00"), new BigDecimal("40.00"), new BigDecimal("80.000"), new BigDecimal("15.000"));
            Product p11 = create("Tomato", "தக்காளி", "8901030000111", "Vegetables", ProductType.LOOSE, Unit.KG, new BigDecimal("25.00"), new BigDecimal("35.00"), new BigDecimal("40.000"), new BigDecimal("10.000"));

            List<Product> saved = productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));

            // Seed sample price history
            PriceHistory h1 = new PriceHistory(saved.get(0), new BigDecimal("50.00"), new BigDecimal("55.00"), new BigDecimal("42.00"), new BigDecimal("45.00"), UpdateMethod.MANUAL, "admin", "Wholesale price adjustment");
            PriceHistory h2 = new PriceHistory(saved.get(1), new BigDecimal("45.00"), new BigDecimal("48.00"), new BigDecimal("38.00"), new BigDecimal("40.00"), UpdateMethod.VOICE, "admin", "Voice updated after arrival of new stock");
            priceHistoryRepository.saveAll(List.of(h1, h2));
            log.info("Sample grocery products and price history seeded successfully!");
        }
    }

    private Product create(String nameEn, String nameTa, String barcode, String cat,
                           ProductType type, Unit unit, BigDecimal purchase, BigDecimal sell,
                           BigDecimal stock, BigDecimal threshold) {
        Product p = new Product();
        p.setNameEn(nameEn);
        p.setNameTa(nameTa);
        p.setBarcode(barcode);
        p.setCategory(cat);
        p.setProductType(type);
        p.setUnit(unit);
        p.setPurchasePrice(purchase);
        p.setSellingPrice(sell);
        p.setStockQuantity(stock);
        p.setLowStockThreshold(threshold);
        p.setActive(true);
        return p;
    }
}
