# Smart Grocery Assistant (ஸ்மார்ட் மளிகை உதவியாளர்)
### Accessible Grocery Shop Management System for a Tamil-Speaking Shopkeeper

---

## 1. Project Overview

The **Smart Grocery Assistant** is a purpose-built grocery management system tailored for small Indian grocery shopkeepers who speak Tamil and are not literate. It addresses key day-to-day retail hurdles:
- **Difficulty reading printed prices on packets** → Solved with high-contrast UI, visual item cards, barcode scanning, and instant Tamil voice price announcements.
- **Remembering hundreds of fluctuating grocery prices** → Managed in a local MySQL database with instant lookup and audit trails.
- **Mental arithmetic for multi-item customer purchases** → Automated billing with large tactile touch keypad and Tamil spoken totals.
- **Decimal pricing for loose goods sold by weight or volume** → Precise `BigDecimal` conversions for kilograms, grams, litres, and millilitres.
- **Price updates by family members** → Supported both through an administrative form and through confirmed voice commands.

---

## 2. Directory Structure

```
Grocery_price/
├── pom.xml                                      # Maven build file (Spring Boot 3.3.5, Java 21)
├── run.bat                                      # Quick startup batch file
├── backup_db.bat                                # MySQL database backup script (mysqldump)
├── restore_db.bat                               # MySQL database restore script
├── src/
│   ├── main/
│   │   ├── java/com/grocery/assistant/
│   │   │   ├── SmartGroceryAssistantApplication.java
│   │   │   ├── entity/                          # JPA Entities
│   │   │   │   ├── User.java                    # Users & roles (ADMIN, SHOPKEEPER)
│   │   │   │   ├── Product.java                 # Products (loose & packaged)
│   │   │   │   ├── PriceHistory.java            # Price audit trail (MANUAL, VOICE)
│   │   │   │   ├── Sale.java                    # Customer bills
│   │   │   │   ├── SaleItem.java                # Frozen historical price snapshots
│   │   │   │   └── enums/                       # Unit, ProductType, Role, UpdateMethod
│   │   │   ├── repository/                      # Spring Data JPA Repositories
│   │   │   ├── dto/                             # Request & Response Transfer Objects
│   │   │   ├── service/                         # Business Logic
│   │   │   │   ├── ProductService.java          # Product catalog & CRUD
│   │   │   │   ├── PriceService.java            # Concurrency-safe price updates
│   │   │   │   ├── VoiceCommandService.java     # NLP speech command parser
│   │   │   │   ├── BillingService.java          # Checkout & frozen price retention
│   │   │   │   ├── TamilVoiceService.java       # Number-to-Tamil words & voice generator
│   │   │   │   ├── UnitConversionService.java   # kg/g & L/ml decimal math
│   │   │   │   └── CustomUserDetailsService.java# Spring Security UserDetails provider
│   │   │   ├── security/                        # SecurityConfig (Role-Based Access Control)
│   │   │   └── controller/                      # REST API Endpoints & Exception Handling
│   │   └── resources/
│   │       ├── application.properties           # Server & MySQL DataSource configuration
│   │       ├── db/migration/
│   │       │   ├── V1__init_schema.sql          # Flyway DDL (users, products, sales, etc.)
│   │       │   └── V2__seed_demo_data.sql       # Initial demonstration products & history
│   │       └── static/                          # Accessible Frontend Single Page Application
│   │           ├── index.html                   # Mobile-first tactile user interface
│   │           ├── app.css                      # High-contrast design system
│   │           └── app.js                       # Tamil Web Speech, Cart & Voice Parsing
│   └── test/
│       ├── java/com/grocery/assistant/
│       │   ├── SmartGroceryAssistantIntegrationTest.java # Spring Security & WebMvc IT
│       │   └── service/
│       │       ├── BillingServiceTest.java      # Verification of required calculation cases
│       │       ├── PriceServiceTest.java        # Audit logging & concurrency tests
│       │       ├── ProductServiceTest.java      # CRUD & barcode duplicate check
│       │       ├── TamilVoiceServiceTest.java   # Tamil compound numbers & phrases
│       │       ├── UnitConversionServiceTest.java # Loose quantity conversion math
│       │       └── VoiceCommandServiceTest.java # Ambiguity & speech parser safety
│       └── resources/
│           └── application.properties           # Test profile using in-memory H2
```

---

## 3. Technology Stack

- **Backend**: Java 21 / 23, Spring Boot 3.3.5, Spring Data JPA, Spring Security, Bean Validation.
- **Database**: MySQL 8.0, Hibernate, Flyway Database Migrations.
- **Frontend**: HTML5, Vanilla CSS3 (Custom Accessible Design System), JavaScript (ES6+).
- **Speech Technologies**:
  - Browser **Web Speech Synthesis API** (`ta-IN`) with phonetic fallback.
  - Browser **Web Speech Recognition API** with fallback text parser simulation.
- **Testing**: JUnit 5, Mockito, Spring Security Test, Spring Boot Test (30 automated tests passing).

---

## 4. Mobile Installation (PWA on Android / iOS)

The application is configured as an **installable Progressive Web App (PWA)**:
1. Ensure your phone and the host computer are connected to the same Wi-Fi network.
2. On your Android phone, open **Google Chrome** and navigate to your computer's IP address (e.g., `http://192.168.1.100:8080`).
3. You will see an **"📲 இன்ஸ்டால் (Install)"** button in the header, or you can tap Chrome's three-dot menu (**⋮**) and tap **"Add to Home screen"** or **"Install App"**.
4. The app icon will appear directly on your phone's home screen.
5. Opening it launches the application in **full-screen standalone mode** (without any browser address bar), looking and feeling exactly like a native Android app!

---

## 5. User Roles & Security

| Role | Username | Password | Permissions |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Full access: Add/edit products, manual and voice price updates, view purchase costs and price audit history, manage inventory. |
| **SHOPKEEPER** | `shopkeeper` | `shop123` | Counter operations: Find prices, hear Tamil prices, barcode scanning, billing keypad, loose product conversions, completing sales. |

---

## 5. Setup & Running Instructions

### Prerequisites
1. **Java JDK 21+** installed and available on PATH (`java -version`).
2. **Maven 3.9+** (installed at `C:\Program Files\apache-maven-3.9.11`).
3. **MySQL Server 8.0** running on `localhost:3306`.

### MySQL Database Configuration
The application connects by default to:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/grocery_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=root
```
You can override these with environment variables if your MySQL credentials differ:
```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

### Running the Application
From the project root:
```powershell
& "C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
```
Or double-click `run.bat`.

Once started, open your web browser at:
👉 **`http://localhost:8080/`**

### Running Automated Tests
```powershell
& "C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd" test
```

---

## 6. Demonstration Data Guide

The Flyway migration `V2__seed_demo_data.sql` automatically populates realistic shop data:
- **Rice (Ponni) / பொன்னி அரிசி**: ₹55.00 / KG (Loose) • Barcode: `8901030000101`
- **Sugar / சர்க்கரை**: ₹48.00 / KG (Loose) • Barcode: `8901030000102`
- **Bath Soap (Lifebuoy) / சோப்**: ₹35.00 / PIECE (Packaged) • Barcode: `8901030000103`
- **Marie Gold Biscuits / மேரி கோல்ட் பிஸ்கட்**: ₹20.00 / PACKET • Barcode: `8901030000104`
- **Refined Sunflower Oil / சமையல் எண்ணெய்**: ₹125.00 / LITRE (Loose) • Barcode: `8901030000105`
- **Toor Dal / துவரம் பருப்பு**: ₹140.00 / KG • Barcode: `8901030000106`
- **Chakra Gold Tea / டீ தூள்**: ₹75.00 / PACKET • Barcode: `8901030000107`
- **Aavin Milk / பால் பாக்கெட்**: ₹30.00 / PACKET • Barcode: `8901030000108`
- **Tata Salt / உப்பு**: ₹15.00 / PACKET • Barcode: `8901030000109`
- **Onion / வெங்காயம்**: ₹40.00 / KG • Barcode: `8901030000110`
- **Tomato / தக்காளி**: ₹35.00 / KG • Barcode: `8901030000111`

---

## 7. Operational Instructions

### A. Selling Products & Billing
1. Tap any product card from the **விற்பனை (Sell)** screen or scan its barcode.
2. The **Tactile Keypad Modal** opens:
   - For loose products (e.g., Rice, Sugar), select **கிலோ (KG)** or **கிராம் (Gram)**.
   - Quick preset chips are available for fast weight entry (250g, 500g, 1kg, 2kg, 5kg).
   - Tap digits on the large numeric pad.
3. Tap **சேர் (Add to Bill)**. The item is added to the bill and Tamil audio confirms addition.
4. Tap **பில் முடி (Complete Sale)**.
5. The completed bill is stored in MySQL, stock is reduced, and the final total is announced in Tamil (e.g., *"மொத்த தொகை நூற்று எண்பத்தைந்து ரூபாய்"*).

### B. Finding a Product Price
1. Navigate to the **விலை அறிதல் (Find Price)** screen.
2. Tap a visual product card, enter a name, or tap a sample barcode chip.
3. The system displays the price in huge green font and announces it in Tamil:
   - *"ஒரு கிலோ பொன்னி அரிசி ஐம்பத்தைந்து ரூபாய்."*
   - *"ஒரு சோப்பின் விலை முப்பத்தைந்து ரூபாய்."*
4. Tap **🔁 திரும்பக் கூறு (Repeat)** to hear the price again at any time.

### C. Updating Prices by Voice (Admin Only)
1. Navigate to **குரல் வழி மாற்றம் (Voice Price Update)**.
2. Tap the large blue microphone button.
3. Speak a command clearly, e.g.:
   - *"Change rice price to 55 rupees per kilogram"*
   - *"Change soap price to 35 rupees per piece"*
   - *"அரிசி விலை 55 ரூபாய் ஆக மாற்று"*
4. **Safety & Confirmation**: The system parses the product and price, compares the old price against the proposed price, and speaks:
   - *"பொன்னி அரிசி விலை ஐம்பத்தைந்து ரூபாய் என்று மாற்றவா?"*
5. The administrator must explicitly tap **✅ உறுதி செய் (Confirm & Update)** before any database update occurs.
6. If the spoken name is ambiguous (e.g. "Change oil price"), matching products are presented for the user to select.

---

## 8. REST API Reference

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `GET` | `/api/products` | SHOPKEEPER, ADMIN | List all active grocery products |
| `GET` | `/api/products/{id}` | SHOPKEEPER, ADMIN | Get product details by ID |
| `GET` | `/api/products/barcode/{barcode}` | SHOPKEEPER, ADMIN | Search product by barcode |
| `GET` | `/api/products/search?query=...` | SHOPKEEPER, ADMIN | Search by English/Tamil name |
| `POST` | `/api/products` | ADMIN | Add new product |
| `PUT` | `/api/products/{id}` | ADMIN | Update product information |
| `DELETE` | `/api/products/{id}` | ADMIN | Soft delete / deactivate product |
| `PATCH` | `/api/products/{id}/price` | ADMIN | Update product price with audit log |
| `GET` | `/api/products/{id}/price-history` | ADMIN | View price history for product |
| `GET` | `/api/price-history` | ADMIN | Paged price audit history |
| `POST` | `/api/voice/parse-price-command` | ADMIN | Parse spoken price command without writing |
| `POST` | `/api/sales` | SHOPKEEPER, ADMIN | Complete sale & snapshot prices |
| `GET` | `/api/sales/{id}` | SHOPKEEPER, ADMIN | Retrieve completed sale details |
| `GET` | `/api/sales` | SHOPKEEPER, ADMIN | View sales history |

---

## 9. Backup & Recovery

### Backup
Run:
```powershell
.\backup_db.bat
```
This generates a timestamped SQL dump in the `backups/` directory using `mysqldump`.

### Restore
To restore from a backup:
```powershell
.\restore_db.bat backups\grocery_db_backup_YYYY-MM-DD_HHMM.sql
```

---

## 10. Low-Cost & Offline Operation Notes

- **Zero Cloud Costs**: Uses local MySQL and native browser speech engines. No paid AI, Whisper, or external cloud speech APIs are required.
- **Offline Reliability**: The core billing, barcode lookups, product catalog, and speech synthesis operate on the local network or offline once browser voice packages are cached.
