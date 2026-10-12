/**
 * Smart Grocery Assistant (ஸ்மார்ட் மளிகை உதவியாளர்)
 * Frontend Application Engine
 */

// Application State
const savedLanguage = (typeof localStorage !== 'undefined' && localStorage.getItem('grocery_lang')) || 'ta';
const state = {
    currentRole: 'ADMIN', // Default to ADMIN so all tabs are visible for testing & demo
    currentUsername: 'admin',
    language: savedLanguage, // 'ta' or 'en'
    products: [],
    categories: [],
    cart: [], // items in active bill
    activeTab: 'sellTab',
    lastSpokenText: '',
    selectedKeypadProduct: null,
    keypadQuantityStr: '1',
    keypadUnit: 'KG',
    pendingVoiceParseResult: null
};

// UI Translations
const i18n = {
    ta: {
        appTitle: "ஸ்மார்ட் மளிகை உதவியாளர்",
        appSubtitle: "தமிழ் மளிகைக் கடை மேலாண்மை",
        tabSell: "விற்பனை",
        tabLookup: "விலை அறிதல்",
        tabManagePrices: "விலை மாற்றம்",
        tabVoicePrice: "குரல் வழி மாற்றம்",
        tabPriceHistory: "விலை வரலாறு",
        tabInventory: "சரக்கு இருப்பு",
        tabSalesHistory: "விற்பனை வரலாறு",
        emptyCart: "பொருட்களைத் தேர்வு செய்யவும்",
        totalText: "மொத்த தொகை (Total):",
        completeSale: "பில் முடி (Complete Sale)",
        speakAgain: "🔁 திரும்பக் கூறு"
    },
    en: {
        appTitle: "Smart Grocery Assistant",
        appSubtitle: "Tamil Grocery Store Manager",
        tabSell: "Sell Products",
        tabLookup: "Find Price",
        tabManagePrices: "Manage Prices",
        tabVoicePrice: "Voice Update",
        tabPriceHistory: "Price History",
        tabInventory: "Inventory",
        tabSalesHistory: "Sales History",
        emptyCart: "No items added yet",
        totalText: "Total Amount:",
        completeSale: "Complete Sale",
        speakAgain: "🔁 Repeat Voice"
    }
};

// ==========================================
// 1. SPEECH SYNTHESIS SERVICE (TAMIL AUDIO)
// ==========================================
class VoiceService {
    constructor() {
        this.synth = window.speechSynthesis;
        this.tamilVoice = null;
        this.englishVoice = null;
        this.hasSpeechSupport = 'speechSynthesis' in window;
        this.initVoices();
    }

    initVoices() {
        if (!this.hasSpeechSupport) return;
        const load = () => {
            const voices = this.synth.getVoices();
            // Look for Tamil voice (ta-IN or ta)
            this.tamilVoice = voices.find(v => v.lang.toLowerCase().startsWith('ta') || v.name.toLowerCase().includes('tamil')) ||
                              voices.find(v => v.lang === 'en-IN') ||
                              voices[0];
            // Look for English voice (en-IN, en-US, en-GB, en)
            this.englishVoice = voices.find(v => v.lang === 'en-IN' || v.name.toLowerCase().includes('india')) ||
                                voices.find(v => v.lang.toLowerCase().startsWith('en-')) ||
                                voices.find(v => v.lang.toLowerCase().startsWith('en')) ||
                                voices[0];
        };
        load();
        if (this.synth.onvoiceschanged !== undefined) {
            this.synth.onvoiceschanged = load;
        }
    }

    speak(text, forceLang = null) {
        if (!text || !text.trim()) return;
        state.lastSpokenText = text;

        const langToUse = forceLang || state.language;

        // Display in announcement banner
        const banner = document.getElementById('speechAnnouncementBanner');
        const textDisplay = document.getElementById('speechTextDisplay');
        const repeatBtn = document.getElementById('repeatAudioBtn');

        if (banner && textDisplay) {
            banner.style.display = 'flex';
            textDisplay.textContent = text;
        }
        if (repeatBtn) {
            repeatBtn.style.display = 'inline-flex';
        }

        if (!this.hasSpeechSupport) {
            console.warn("Browser does not support Speech Synthesis API. Text displayed in UI banner.");
            return;
        }

        try {
            this.synth.cancel(); // Stop any pending speech
            const utterance = new SpeechSynthesisUtterance(text);
            if (langToUse === 'ta') {
                if (this.tamilVoice) {
                    utterance.voice = this.tamilVoice;
                }
                utterance.lang = (this.tamilVoice && this.tamilVoice.lang) ? this.tamilVoice.lang : 'ta-IN';
            } else {
                if (this.englishVoice) {
                    utterance.voice = this.englishVoice;
                }
                utterance.lang = (this.englishVoice && this.englishVoice.lang) ? this.englishVoice.lang : 'en-IN';
            }
            utterance.rate = 0.95;
            utterance.pitch = 1.0;
            this.synth.speak(utterance);
        } catch (e) {
            console.error("Speech synthesis failed:", e);
        }
    }

    repeatLast() {
        if (state.lastSpokenText) {
            this.speak(state.lastSpokenText);
        }
    }
}

const voiceService = new VoiceService();

// ==========================================
// 2. SPEECH RECOGNITION SERVICE (VOICE INPUT)
// ==========================================
class SpeechRecognitionService {
    constructor() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        this.supported = !!SpeechRecognition;
        this.recognition = this.supported ? new SpeechRecognition() : null;
        this.isListening = false;

        if (this.supported) {
            this.recognition.continuous = false;
            this.recognition.interimResults = false;
            this.recognition.lang = 'en-IN'; // Default to Indian English / bilingual

            this.recognition.onstart = () => {
                this.isListening = true;
                this.updateUI(true);
            };

            this.recognition.onresult = (event) => {
                const transcript = event.results[0][0].transcript;
                this.handleRecognizedText(transcript);
            };

            this.recognition.onerror = (event) => {
                console.warn("Speech recognition error:", event.error);
                this.isListening = false;
                this.updateUI(false);
                showAlert("Speech error: " + event.error + ". You can also type the command below.", "warning");
            };

            this.recognition.onend = () => {
                this.isListening = false;
                this.updateUI(false);
            };
        }
    }

    startListening(targetLang = 'en-IN') {
        if (!this.supported) {
            showAlert("Speech Recognition is not supported by your browser. Please use the text input below.", "warning");
            document.getElementById('voiceCommandManualInput')?.focus();
            return;
        }
        try {
            this.recognition.lang = targetLang;
            this.recognition.start();
        } catch (e) {
            console.error("Recognition start failed:", e);
        }
    }

    stopListening() {
        if (this.recognition && this.isListening) {
            this.recognition.stop();
        }
    }

    updateUI(listening) {
        const btn = document.getElementById('voiceMicTriggerBtn');
        const wave = document.getElementById('micListeningWave');
        const statusText = document.getElementById('voiceMicStatusText');

        if (btn) {
            if (listening) {
                btn.classList.add('listening');
                if (statusText) statusText.textContent = "கேட்கிறது... பேசுங்கள் (Listening...)";
                if (wave) wave.style.display = 'flex';
            } else {
                btn.classList.remove('listening');
                if (statusText) statusText.textContent = "பேச தட்டவும் (Tap to Speak)";
                if (wave) wave.style.display = 'none';
            }
        }
    }

    handleRecognizedText(transcript) {
        const display = document.getElementById('voiceTranscriptText');
        if (display) display.textContent = `"${transcript}"`;

        const manualInput = document.getElementById('voiceCommandManualInput');
        if (manualInput) manualInput.value = transcript;

        // Automatically trigger parsing with backend
        parseVoiceCommand(transcript);
    }
}

const speechRecognitionService = new SpeechRecognitionService();

// ==========================================
// 3. API CLIENT (REST CALLS)
// ==========================================
const api = {
    getAuthHeader() {
        // HTTP Basic credentials based on selected role
        const creds = state.currentRole === 'ADMIN' ? 'admin:admin123' : 'shopkeeper:shop123';
        return {
            'Authorization': 'Basic ' + btoa(creds),
            'Content-Type': 'application/json'
        };
    },

    async get(endpoint) {
        const res = await fetch(endpoint, { credentials: 'include', headers: this.getAuthHeader() });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Error ' + res.status);
        }
        return res.json();
    },

    async post(endpoint, data) {
        const res = await fetch(endpoint, {
            method: 'POST',
            credentials: 'include',
            headers: this.getAuthHeader(),
            body: JSON.stringify(data)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Error ' + res.status);
        }
        return res.json();
    },

    async put(endpoint, data) {
        const res = await fetch(endpoint, {
            method: 'PUT',
            credentials: 'include',
            headers: this.getAuthHeader(),
            body: JSON.stringify(data)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Error ' + res.status);
        }
        return res.json();
    },

    async patch(endpoint, data) {
        const res = await fetch(endpoint, {
            method: 'PATCH',
            credentials: 'include',
            headers: this.getAuthHeader(),
            body: JSON.stringify(data)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Error ' + res.status);
        }
        return res.json();
    },

    async delete(endpoint) {
        const res = await fetch(endpoint, {
            method: 'DELETE',
            credentials: 'include',
            headers: this.getAuthHeader()
        });
        if (!res.ok && res.status !== 204) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.message || 'Error ' + res.status);
        }
        return true;
    }
};

// ==========================================
// 4. DATA LOADING & INITIALIZATION
// ==========================================
async function loadProducts() {
    try {
        const prods = await api.get('/api/products');
        state.products = prods;

        // Categories extraction
        const catSet = new Set(prods.map(p => p.category));
        state.categories = Array.from(catSet);

        renderCategoryPills();
        renderBillProductGrid(prods);
        renderVisualLookupGrid(prods);
        populateManagePriceDropdown(prods);
        renderInventoryTable(prods);
    } catch (e) {
        console.error("Failed to load products:", e);
        showAlert("Failed to load products: " + e.message, "error");
    }
}

// ==========================================
// 5. BILLING & CART FUNCTIONS
// ==========================================
function renderCategoryPills() {
    const container = document.getElementById('billCategoryPills');
    if (!container) return;
    container.innerHTML = `<button class="pill active" data-category="ALL">அனைத்தும் (All)</button>`;
    state.categories.forEach(cat => {
        const btn = document.createElement('button');
        btn.className = 'pill';
        btn.dataset.category = cat;
        btn.textContent = cat;
        btn.onclick = () => {
            document.querySelectorAll('#billCategoryPills .pill').forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            filterBillProductsByCategory(cat);
        };
        container.appendChild(btn);
    });

    container.querySelector('[data-category="ALL"]').onclick = () => {
        document.querySelectorAll('#billCategoryPills .pill').forEach(p => p.classList.remove('active'));
        container.querySelector('[data-category="ALL"]').classList.add('active');
        renderBillProductGrid(state.products);
    };
}

function filterBillProductsByCategory(cat) {
    if (cat === 'ALL') {
        renderBillProductGrid(state.products);
    } else {
        const filtered = state.products.filter(p => p.category === cat);
        renderBillProductGrid(filtered);
    }
}

function renderBillProductGrid(products) {
    const grid = document.getElementById('billProductGrid');
    if (!grid) return;
    grid.innerHTML = '';

    const isEn = (state.language === 'en');
    if (products.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 20px; color: #94a3b8;">${isEn ? 'No products found' : 'பொருட்கள் எதுவும் இல்லை (No products found)'}</div>`;
        return;
    }

    products.forEach(p => {
        const card = document.createElement('div');
        card.className = 'product-card';
        const primaryTitle = isEn ? (p.nameEn || p.nameTa) : (p.nameTa || p.nameEn);
        const secondaryTitle = isEn ? (p.nameTa || '') : p.nameEn;
        card.innerHTML = `
            <div>
                <div class="card-top">
                    <span class="card-unit-badge">${p.unit}</span>
                    <span style="font-size: 0.75rem; color: #94a3b8;">#${p.id}</span>
                </div>
                <div class="card-title-ta">${primaryTitle}</div>
                <div class="card-title-en">${secondaryTitle}</div>
            </div>
            <div class="card-price-row">
                <span class="card-price-value">₹${p.sellingPrice.toFixed(2)}</span>
                <span class="card-add-icon">＋</span>
            </div>
        `;
        card.onclick = () => openQuantityModal(p);
        grid.appendChild(card);
    });
}

// Quantity Keypad Modal
function openQuantityModal(product) {
    state.selectedKeypadProduct = product;
    state.keypadQuantityStr = '1';
    state.keypadUnit = (product.unit === 'GRAM') ? 'GRAM' : ((product.unit === 'ML') ? 'ML' : (product.unit || 'PIECE'));

    document.getElementById('keypadProductNameTa').textContent = product.nameTa || product.nameEn;
    document.getElementById('keypadProductPriceInfo').textContent = `₹${product.sellingPrice.toFixed(2)} / ${product.unit}`;

    // Loose unit toggles
    const unitRow = document.getElementById('keypadUnitSelector');
    const quickChips = document.getElementById('keypadQuickChips');

    if (product.productType === 'LOOSE' && (product.unit === 'KG' || product.unit === 'GRAM')) {
        unitRow.style.display = 'flex';
        quickChips.style.display = 'flex';
        unitRow.innerHTML = `
            <button class="unit-toggle ${state.keypadUnit === 'KG' ? 'active' : ''}" onclick="toggleKeypadUnit('KG')">கிலோ (KG)</button>
            <button class="unit-toggle ${state.keypadUnit === 'GRAM' ? 'active' : ''}" onclick="toggleKeypadUnit('GRAM')">கிராம் (Gram)</button>
        `;
    } else if (product.productType === 'LOOSE' && (product.unit === 'LITRE' || product.unit === 'ML')) {
        unitRow.style.display = 'flex';
        quickChips.style.display = 'none';
        unitRow.innerHTML = `
            <button class="unit-toggle ${state.keypadUnit === 'LITRE' ? 'active' : ''}" onclick="toggleKeypadUnit('LITRE')">லிட்டர் (Litre)</button>
            <button class="unit-toggle ${state.keypadUnit === 'ML' ? 'active' : ''}" onclick="toggleKeypadUnit('ML')">மில்லி (ML)</button>
        `;
    } else {
        unitRow.style.display = 'none';
        quickChips.style.display = 'none';
    }

    updateKeypadDisplay();
    document.getElementById('quantityModal').style.display = 'flex';
}

function closeQuantityModal() {
    document.getElementById('quantityModal').style.display = 'none';
    state.selectedKeypadProduct = null;
}

function toggleKeypadUnit(unit) {
    state.keypadUnit = unit;
    const toggles = document.querySelectorAll('#keypadUnitSelector .unit-toggle');
    toggles.forEach(t => t.classList.toggle('active', t.textContent.includes(unit)));
    updateKeypadDisplay();
}

function setKeypadQuantity(num, unit) {
    state.keypadQuantityStr = num.toString();
    if (unit) toggleKeypadUnit(unit);
    updateKeypadDisplay();
}

function appendKeypadDigit(digit) {
    if (digit === '.' && state.keypadQuantityStr.includes('.')) return;
    if (state.keypadQuantityStr === '0' && digit !== '.') {
        state.keypadQuantityStr = digit;
    } else {
        state.keypadQuantityStr += digit;
    }
    updateKeypadDisplay();
}

function backspaceKeypad() {
    state.keypadQuantityStr = state.keypadQuantityStr.slice(0, -1);
    if (!state.keypadQuantityStr) state.keypadQuantityStr = '0';
    updateKeypadDisplay();
}

function updateKeypadDisplay() {
    const qty = parseFloat(state.keypadQuantityStr) || 0;
    document.getElementById('keypadQtyValue').textContent = state.keypadQuantityStr;
    document.getElementById('keypadQtyUnit').textContent = state.keypadUnit;

    const p = state.selectedKeypadProduct;
    if (!p) return;

    let subtotal = 0;
    if (p.unit === 'KG' && state.keypadUnit === 'GRAM') {
        subtotal = (qty / 1000) * p.sellingPrice;
    } else if (p.unit === 'LITRE' && state.keypadUnit === 'ML') {
        subtotal = (qty / 1000) * p.sellingPrice;
    } else {
        subtotal = qty * p.sellingPrice;
    }

    document.getElementById('keypadSubtotalPreview').textContent = `₹${subtotal.toFixed(2)}`;
}

// Add Item from Keypad to Bill Cart
document.getElementById('keypadConfirmAddBtn')?.addEventListener('click', () => {
    const qty = parseFloat(state.keypadQuantityStr) || 0;
    if (qty <= 0) {
        showAlert("Please enter a valid quantity greater than zero.", "warning");
        return;
    }
    const p = state.selectedKeypadProduct;
    if (!p) return;

    let subtotal = 0;
    if (p.unit === 'KG' && state.keypadUnit === 'GRAM') {
        subtotal = (qty / 1000) * p.sellingPrice;
    } else if (p.unit === 'LITRE' && state.keypadUnit === 'ML') {
        subtotal = (qty / 1000) * p.sellingPrice;
    } else {
        subtotal = qty * p.sellingPrice;
    }

    // Add to cart
    state.cart.push({
        product: p,
        quantity: qty,
        unit: state.keypadUnit,
        clientPriceSnapshot: p.sellingPrice,
        subtotal: subtotal
    });

    closeQuantityModal();
    renderCart();

    // Spoken feedback for the added product
    const isEn = (state.language === 'en');
    const itemAnnouncement = isEn
        ? `${p.nameEn || p.nameTa} added to bill.`
        : `${p.nameTa || p.nameEn} பில்லில் சேர்க்கப்பட்டது.`;
    voiceService.speak(itemAnnouncement);
});

function renderCart() {
    const list = document.getElementById('cartItemsList');
    const emptyMsg = document.getElementById('emptyCartMsg');
    const countDisplay = document.getElementById('cartItemCount');
    const totalDisplay = document.getElementById('cartTotalDisplay');
    const tamilTotalWords = document.getElementById('cartTamilTotalWords');
    const isEn = (state.language === 'en');

    if (state.cart.length === 0) {
        list.innerHTML = '';
        list.appendChild(emptyMsg);
        countDisplay.textContent = '0';
        totalDisplay.textContent = '₹0.00';
        tamilTotalWords.textContent = isEn ? 'Zero Rupees' : 'பூஜ்ஜியம் ரூபாய்';
        return;
    }

    list.innerHTML = '';
    let total = 0;

    state.cart.forEach((item, index) => {
        total += item.subtotal;
        const row = document.createElement('div');
        row.className = 'cart-row';
        const itemTitle = isEn ? (item.product.nameEn || item.product.nameTa) : (item.product.nameTa || item.product.nameEn);
        row.innerHTML = `
            <div class="cart-item-info">
                <h4>${itemTitle}</h4>
                <small>${item.quantity} ${item.unit} × ₹${item.clientPriceSnapshot.toFixed(2)}</small>
            </div>
            <div class="cart-item-actions">
                <span class="cart-item-subtotal">₹${item.subtotal.toFixed(2)}</span>
                <button class="cart-delete-btn" onclick="removeFromCart(${index})" title="Remove">✕</button>
            </div>
        `;
        list.appendChild(row);
    });

    countDisplay.textContent = state.cart.length;
    totalDisplay.textContent = `₹${total.toFixed(2)}`;

    // Calculate running words in active language
    tamilTotalWords.textContent = isEn ? formatRupeesInEnglish(total) : formatRupeesInTamil(total);
}

function removeFromCart(index) {
    state.cart.splice(index, 1);
    renderCart();
}

document.getElementById('clearBillBtn')?.addEventListener('click', () => {
    if (state.cart.length > 0 && confirm("பில்லை அழிக்கவா? (Clear current bill?)")) {
        state.cart = [];
        renderCart();
    }
});

// Complete Sale (Checkout)
document.getElementById('completeSaleBtn')?.addEventListener('click', async () => {
    if (state.cart.length === 0) {
        showAlert("பில் காலியாக உள்ளது. பொருட்களைச் சேர்க்கவும். (Bill is empty)", "warning");
        return;
    }

    const payload = {
        saleNumber: null, // Generated by backend
        items: state.cart.map(i => ({
            productId: i.product.id,
            quantity: i.quantity,
            unit: i.unit,
            clientPriceSnapshot: i.clientPriceSnapshot
        })),
        discount: 0.00,
        paymentMode: 'CASH'
    };

    try {
        const result = await api.post('/api/sales', payload);

        // Show receipt dialog
        document.getElementById('receiptBillNumber').textContent = result.saleNumber;
        document.getElementById('receiptTotalAmount').textContent = `₹${result.totalAmount.toFixed(2)}`;
        const isEn = (state.language === 'en');
        const billSpoken = isEn
            ? `Total bill amount is ${result.totalAmount.toFixed(0)} rupees.`
            : (result.spokenTamilTotal || `மொத்த தொகை ${result.totalAmount.toFixed(0)} ரூபாய்.`);
        document.getElementById('receiptTamilWords').textContent = billSpoken;

        const itemsBody = document.getElementById('receiptItemsTableBody');
        itemsBody.innerHTML = '';
        result.items.forEach(it => {
            const tr = document.createElement('tr');
            const itemName = isEn ? (it.productNameEn || it.productNameTa) : (it.productNameTa || it.productNameEn);
            tr.innerHTML = `
                <td><strong>${itemName}</strong></td>
                <td>${it.quantity} ${it.unitCode}</td>
                <td>₹${it.unitPriceSnapshot.toFixed(2)}</td>
                <td><strong>₹${it.subtotal.toFixed(2)}</strong></td>
            `;
            itemsBody.appendChild(tr);
        });

        document.getElementById('receiptModal').style.display = 'flex';

        // Speak the final bill total in active language!
        voiceService.speak(billSpoken);

        // Reset cart
        state.cart = [];
        renderCart();

        // Refresh inventory in background
        loadProducts();
    } catch (e) {
        showAlert("Failed to complete sale: " + e.message, "error");
    }
});

function closeReceiptModal() {
    document.getElementById('receiptModal').style.display = 'none';
}

document.getElementById('receiptSpeakAgainBtn')?.addEventListener('click', () => {
    const text = document.getElementById('receiptTamilWords').textContent;
    voiceService.speak(text);
});

// Search in billing
document.getElementById('billProductSearch')?.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    if (!q) {
        renderBillProductGrid(state.products);
        return;
    }
    const filtered = state.products.filter(p =>
        (p.nameEn && p.nameEn.toLowerCase().includes(q)) ||
        (p.nameTa && p.nameTa.toLowerCase().includes(q)) ||
        (p.barcode && p.barcode.includes(q))
    );
    renderBillProductGrid(filtered);
});

// Simulated Barcode Scan in Bill
document.getElementById('billBarcodeBtn')?.addEventListener('click', () => {
    const code = prompt("பார்-கோட் எண்ணை உள்ளிடவும் (Enter or scan barcode):", "8901030000101");
    if (code) {
        const found = state.products.find(p => p.barcode === code.trim());
        if (found) {
            openQuantityModal(found);
        } else {
            showAlert("பார்-கோடுக்குரிய பொருள் காணப்படவில்லை: " + code, "warning");
        }
    }
});

// ==========================================
// 6. FIND PRODUCT PRICE (LOOKUP TAB)
// ==========================================
function renderVisualLookupGrid(products) {
    const grid = document.getElementById('lookupGrid');
    if (!grid) return;
    grid.innerHTML = '';
    const isEn = (state.language === 'en');
    products.forEach(p => {
        const card = document.createElement('div');
        card.className = 'product-card';
        const primaryTitle = isEn ? (p.nameEn || p.nameTa) : (p.nameTa || p.nameEn);
        const secondaryTitle = isEn ? (p.nameTa || '') : p.nameEn;
        card.innerHTML = `
            <div>
                <div class="card-top">
                    <span class="card-unit-badge">${p.unit}</span>
                    <span style="font-size: 1.2rem;">🔊</span>
                </div>
                <div class="card-title-ta">${primaryTitle}</div>
                <div class="card-title-en">${secondaryTitle}</div>
            </div>
            <div class="card-price-row">
                <span class="card-price-value">₹${p.sellingPrice.toFixed(2)}</span>
            </div>
        `;
        card.onclick = () => showPriceHero(p);
        grid.appendChild(card);
    });
}

function showPriceHero(product) {
    const card = document.getElementById('lookupResultCard');
    if (!card) return;

    const isEn = (state.language === 'en');
    card.style.display = 'block';
    document.getElementById('lookupUnitBadge').textContent = product.unit;
    document.getElementById('lookupProductNameTa').textContent = isEn ? (product.nameEn || product.nameTa) : (product.nameTa || product.nameEn);
    document.getElementById('lookupProductNameEn').textContent = isEn ? (product.nameTa || '') : product.nameEn;
    document.getElementById('lookupPriceAmount').textContent = `₹${product.sellingPrice.toFixed(2)}`;

    const spokenText = isEn
        ? `${product.nameEn || product.nameTa}, price is ${product.sellingPrice.toFixed(0)} rupees per ${product.unit}.`
        : (product.spokenTamilPrice || `${product.nameTa} ஒரு ${product.unit} ${product.sellingPrice.toFixed(0)} ரூபாய்.`);

    document.getElementById('lookupPriceTamilWords').textContent = spokenText;

    // Automatically speak the price in active language!
    voiceService.speak(spokenText);

    document.getElementById('lookupSpeakBtn').onclick = () => {
        voiceService.speak(spokenText);
    };

    document.getElementById('lookupAddToBillBtn').onclick = () => {
        openQuantityModal(product);
        switchToTab('sellTab');
    };

    card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function simulateBarcodeLookup(barcode) {
    const found = state.products.find(p => p.barcode === barcode);
    if (found) {
        showPriceHero(found);
    } else {
        showAlert("Barcode not found: " + barcode, "warning");
    }
}

document.getElementById('lookupSearchInput')?.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    if (!q) {
        renderVisualLookupGrid(state.products);
        return;
    }
    const filtered = state.products.filter(p =>
        (p.nameEn && p.nameEn.toLowerCase().includes(q)) ||
        (p.nameTa && p.nameTa.toLowerCase().includes(q)) ||
        (p.barcode && p.barcode.includes(q))
    );
    renderVisualLookupGrid(filtered);
    if (filtered.length === 1) {
        showPriceHero(filtered[0]);
    }
});

// Voice search in Price Lookup
document.getElementById('lookupVoiceSearchBtn')?.addEventListener('click', () => {
    speechRecognitionService.startListening('ta-IN');
});

// ==========================================
// 7. MANAGE PRICES (ADMIN MANUAL)
// ==========================================
function populateManagePriceDropdown(products) {
    const select = document.getElementById('priceProductSelect');
    if (!select) return;
    select.innerHTML = `<option value="">-- பொருளைத் தேர்ந்தெடுக்கவும் (Choose Product) --</option>`;
    products.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.id;
        opt.textContent = `${p.nameTa || p.nameEn} (${p.nameEn}) - ₹${p.sellingPrice.toFixed(2)}/${p.unit}`;
        select.appendChild(opt);
    });

    select.onchange = () => {
        const prodId = parseInt(select.value);
        const p = state.products.find(x => x.id === prodId);
        if (p) {
            document.getElementById('currentPriceDisplay').textContent = `₹${p.sellingPrice.toFixed(2)}`;
            document.getElementById('currentUnitDisplay').textContent = p.unit;
            document.getElementById('newSellingPriceInput').value = p.sellingPrice.toFixed(2);
            document.getElementById('newPurchasePriceInput').value = p.purchasePrice.toFixed(2);
        } else {
            document.getElementById('currentPriceDisplay').textContent = `₹0.00`;
            document.getElementById('currentUnitDisplay').textContent = `-`;
        }
    };
}

document.getElementById('saveManualPriceBtn')?.addEventListener('click', async () => {
    const select = document.getElementById('priceProductSelect');
    const prodId = parseInt(select?.value);
    if (!prodId) {
        showAlert("Please select a product first.", "warning");
        return;
    }
    const product = state.products.find(p => p.id === prodId);
    if (!product) return;

    const newSelling = parseFloat(document.getElementById('newSellingPriceInput').value);
    const newPurchase = parseFloat(document.getElementById('newPurchasePriceInput').value) || 0;
    const reason = document.getElementById('priceChangeReason').value;

    if (!newSelling || newSelling <= 0) {
        showAlert("New selling price must be greater than zero.", "warning");
        return;
    }

    if (!confirm(`பொருள்: ${product.nameTa || product.nameEn}\nபழைய விலை: ₹${product.sellingPrice.toFixed(2)}\nபுதிய விலை: ₹${newSelling.toFixed(2)}\n\nவிலையை மாற்றவா? (Confirm price update?)`)) {
        return;
    }

    const payload = {
        newSellingPrice: newSelling,
        newPurchasePrice: newPurchase,
        updateMethod: 'MANUAL',
        changeReason: reason || 'Manual price update by admin',
        expectedVersion: product.version
    };

    try {
        const updated = await api.patch(`/api/products/${prodId}/price`, payload);
        showAlert(`விலை வெற்றிகரமாக மாற்றப்பட்டது! (Price updated to ₹${updated.sellingPrice.toFixed(2)})`, "success");

        // Voice announcement
        voiceService.speak(updated.spokenTamilPrice);

        // Reload data
        await loadProducts();
        select.value = updated.id;
        select.onchange();
    } catch (e) {
        showAlert("Price update failed: " + e.message, "error");
    }
});

// ==========================================
// 8. VOICE-BASED PRICE UPDATE (ADMIN)
// ==========================================
document.getElementById('voiceMicTriggerBtn')?.addEventListener('click', () => {
    speechRecognitionService.startListening('en-IN');
});

document.getElementById('parseVoiceCommandManualBtn')?.addEventListener('click', () => {
    const txt = document.getElementById('voiceCommandManualInput')?.value;
    if (txt) {
        parseVoiceCommand(txt);
    } else {
        showAlert("Please enter a command text to parse.", "warning");
    }
});

function applySampleVoice(commandText) {
    const manualInput = document.getElementById('voiceCommandManualInput');
    if (manualInput) manualInput.value = commandText;
    parseVoiceCommand(commandText);
}

async function parseVoiceCommand(commandText) {
    try {
        const res = await api.post('/api/voice/parse-price-command', { spokenText: commandText });

        const reviewCard = document.getElementById('voiceReviewCard');
        const ambiguityCard = document.getElementById('ambiguityCard');

        if (res.ambiguous) {
            // Multiple products matched
            reviewCard.style.display = 'none';
            ambiguityCard.style.display = 'block';

            const candGrid = document.getElementById('ambiguityCandidateGrid');
            candGrid.innerHTML = '';
            res.matchingCandidates.forEach(cand => {
                const b = document.createElement('button');
                b.className = 'pill';
                b.style.padding = '12px';
                b.innerHTML = `<strong>${cand.nameTa || cand.nameEn}</strong> (${cand.nameEn})<br>₹${cand.sellingPrice.toFixed(2)} / ${cand.unit}`;
                b.onclick = () => {
                    // Re-bind to this selected candidate
                    res.productId = cand.id;
                    res.productNameEn = cand.nameEn;
                    res.productNameTa = cand.nameTa;
                    res.currentSellingPrice = cand.sellingPrice;
                    res.unit = cand.unit;
                    res.ambiguous = false;
                    res.matched = true;
                    showVoiceConfirmationModal(res);
                };
                candGrid.appendChild(b);
            });

            showAlert(res.errorMessage || "Multiple products found. Please pick the intended one.", "warning");
            return;
        }

        if (!res.matched) {
            reviewCard.style.display = 'none';
            ambiguityCard.style.display = 'none';
            showAlert(res.errorMessage || "Could not parse voice command.", "error");
            return;
        }

        // Single clean match!
        ambiguityCard.style.display = 'none';
        showVoiceConfirmationModal(res);
    } catch (e) {
        showAlert("Voice parsing failed: " + e.message, "error");
    }
}

function showVoiceConfirmationModal(res) {
    state.pendingVoiceParseResult = res;

    const reviewCard = document.getElementById('voiceReviewCard');
    reviewCard.style.display = 'block';

    document.getElementById('reviewProductName').textContent = `${res.productNameTa || res.productNameEn} (${res.productNameEn})`;
    document.getElementById('reviewUnit').textContent = res.unit;
    document.getElementById('reviewOldPrice').textContent = `₹${res.currentSellingPrice.toFixed(2)}`;
    document.getElementById('reviewNewPrice').textContent = `₹${res.proposedSellingPrice.toFixed(2)}`;
    document.getElementById('reviewTamilPrompt').textContent = res.spokenConfirmationTamil;

    // Spoken question in Tamil for confirmation!
    voiceService.speak(res.spokenConfirmationTamil);

    reviewCard.scrollIntoView({ behavior: 'smooth' });
}

document.getElementById('confirmVoiceUpdateBtn')?.addEventListener('click', async () => {
    const res = state.pendingVoiceParseResult;
    if (!res) return;

    const payload = {
        newSellingPrice: res.proposedSellingPrice,
        updateMethod: 'VOICE',
        changeReason: 'Voice command: ' + res.recognizedText
    };

    try {
        const updated = await api.patch(`/api/products/${res.productId}/price`, payload);
        document.getElementById('voiceReviewCard').style.display = 'none';
        state.pendingVoiceParseResult = null;

        showAlert(`விலை குரல் வழியே மாற்றப்பட்டது! (${updated.nameTa || updated.nameEn} ₹${updated.sellingPrice.toFixed(2)})`, "success");

        // Spoken confirmation of success
        voiceService.speak(`${updated.nameTa || updated.nameEn} விலை மாற்றப்பட்டது.`);

        // Reload product catalog
        await loadProducts();
    } catch (e) {
        showAlert("Voice price update confirmation failed: " + e.message, "error");
    }
});

document.getElementById('cancelVoiceUpdateBtn')?.addEventListener('click', () => {
    document.getElementById('voiceReviewCard').style.display = 'none';
    state.pendingVoiceParseResult = null;
    showAlert("குரல் வழி விலை மாற்றம் ரத்து செய்யப்பட்டது (Voice update cancelled)", "warning");
});

// ==========================================
// 9. PRICE HISTORY TAB
// ==========================================
async function loadPriceHistory() {
    try {
        const data = await api.get('/api/price-history?size=50');
        const tbody = document.getElementById('priceHistoryTableBody');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #94a3b8;">வரலாறு பதிவுகள் இல்லை (No history records)</td></tr>`;
            return;
        }

        data.content.forEach(h => {
            const tr = document.createElement('tr');
            const dt = new Date(h.createdAt).toLocaleString('en-IN');
            const badgeClass = h.updateMethod === 'VOICE' ? 'badge-voice' : 'badge-manual';
            tr.innerHTML = `
                <td>${dt}</td>
                <td><strong>${h.productNameTa || h.productNameEn}</strong> <small>(${h.productNameEn})</small></td>
                <td>₹${h.oldSellingPrice.toFixed(2)}</td>
                <td><strong style="color: var(--success);">₹${h.newSellingPrice.toFixed(2)}</strong></td>
                <td><span class="badge ${badgeClass}">${h.updateMethod}</span></td>
                <td>${h.adminUsername}</td>
                <td>${h.changeReason || '-'}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Failed to load price history:", e);
    }
}

document.getElementById('refreshPriceHistoryBtn')?.addEventListener('click', loadPriceHistory);

// ==========================================
// 10. INVENTORY MANAGEMENT (ADMIN)
// ==========================================
function renderInventoryTable(products) {
    const tbody = document.getElementById('inventoryTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    products.forEach(p => {
        const tr = document.createElement('tr');
        const isLow = p.stockQuantity <= p.lowStockThreshold;
        tr.innerHTML = `
            <td>
                <strong>${p.nameTa || p.nameEn}</strong><br>
                <small>${p.nameEn} ${p.barcode ? '• ' + p.barcode : ''}</small>
            </td>
            <td>${p.category}</td>
            <td>${p.productType}</td>
            <td>${p.unit}</td>
            <td><strong>₹${p.sellingPrice.toFixed(2)}</strong></td>
            <td>₹${p.purchasePrice.toFixed(2)}</td>
            <td>
                <span style="font-weight: 700; color: ${isLow ? 'var(--danger)' : 'var(--text-primary)'}">
                    ${p.stockQuantity} ${isLow ? '⚠️' : ''}
                </span>
            </td>
            <td>
                <button class="pill" onclick="openEditProductModal(${p.id})">✏️ மாற்று</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

document.getElementById('openAddProductModalBtn')?.addEventListener('click', () => {
    document.getElementById('prodFormId').value = '';
    document.getElementById('productModalTitle').textContent = 'புதிய பொருள் சேர் (Add New Product)';
    document.getElementById('prodFormNameTa').value = '';
    document.getElementById('prodFormNameEn').value = '';
    document.getElementById('prodFormBarcode').value = '';
    document.getElementById('prodFormCategory').value = '';
    document.getElementById('prodFormType').value = 'PACKAGED';
    document.getElementById('prodFormUnit').value = 'KG';
    document.getElementById('prodFormSellingPrice').value = '';
    document.getElementById('prodFormPurchasePrice').value = '0.00';
    document.getElementById('prodFormStock').value = '50.00';
    document.getElementById('prodFormLowStock').value = '5.00';
    document.getElementById('productModal').style.display = 'flex';
});

function openEditProductModal(id) {
    const p = state.products.find(x => x.id === id);
    if (!p) return;
    document.getElementById('prodFormId').value = p.id;
    document.getElementById('productModalTitle').textContent = 'பொருள் விவரம் திருத்து (Edit Product)';
    document.getElementById('prodFormNameTa').value = p.nameTa || '';
    document.getElementById('prodFormNameEn').value = p.nameEn;
    document.getElementById('prodFormBarcode').value = p.barcode || '';
    document.getElementById('prodFormCategory').value = p.category;
    document.getElementById('prodFormType').value = p.productType;
    document.getElementById('prodFormUnit').value = p.unit;
    document.getElementById('prodFormSellingPrice').value = p.sellingPrice;
    document.getElementById('prodFormPurchasePrice').value = p.purchasePrice;
    document.getElementById('prodFormStock').value = p.stockQuantity;
    document.getElementById('prodFormLowStock').value = p.lowStockThreshold;
    document.getElementById('productModal').style.display = 'flex';
}

function closeProductModal() {
    document.getElementById('productModal').style.display = 'none';
}

document.getElementById('saveProductFormBtn')?.addEventListener('click', async () => {
    const id = document.getElementById('prodFormId').value;
    const nameEn = document.getElementById('prodFormNameEn').value.trim();
    const nameTa = document.getElementById('prodFormNameTa').value.trim();
    const category = document.getElementById('prodFormCategory').value.trim();
    const barcode = document.getElementById('prodFormBarcode').value.trim();
    const productType = document.getElementById('prodFormType').value;
    const unit = document.getElementById('prodFormUnit').value;
    const sellingPrice = parseFloat(document.getElementById('prodFormSellingPrice').value);
    const purchasePrice = parseFloat(document.getElementById('prodFormPurchasePrice').value) || 0;
    const stockQuantity = parseFloat(document.getElementById('prodFormStock').value) || 0;
    const lowStockThreshold = parseFloat(document.getElementById('prodFormLowStock').value) || 5;

    if (!nameEn || !category || !sellingPrice || sellingPrice <= 0) {
        showAlert("Please fill in all required fields with valid prices.", "warning");
        return;
    }

    const payload = {
        nameEn, nameTa, category,
        barcode: barcode || null,
        productType, unit,
        sellingPrice, purchasePrice,
        stockQuantity, lowStockThreshold,
        active: true
    };

    try {
        if (id) {
            await api.put(`/api/products/${id}`, payload);
            showAlert("பொருள் விவரம் புதுப்பிக்கப்பட்டது (Product updated)", "success");
        } else {
            await api.post('/api/products', payload);
            showAlert("புதிய பொருள் சேர்க்கப்பட்டது (Product added)", "success");
        }
        closeProductModal();
        await loadProducts();
    } catch (e) {
        showAlert("Save product failed: " + e.message, "error");
    }
});

// ==========================================
// 11. SALES HISTORY TAB
// ==========================================
async function loadSalesHistory() {
    try {
        const data = await api.get('/api/sales?size=50');
        const tbody = document.getElementById('salesTableBody');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #94a3b8;">விற்பனைகள் எதுவும் இல்லை (No completed sales)</td></tr>`;
            return;
        }

        data.content.forEach(s => {
            const tr = document.createElement('tr');
            const dt = new Date(s.createdAt).toLocaleString('en-IN');
            tr.innerHTML = `
                <td><strong>${s.saleNumber}</strong></td>
                <td>${dt}</td>
                <td>${s.items.length} பொருட்கள்</td>
                <td><strong style="color: var(--success); font-size: 1.1rem;">₹${s.totalAmount.toFixed(2)}</strong></td>
                <td>${s.paymentMode}</td>
                <td>${s.cashierUsername}</td>
                <td>
                    <button class="pill" onclick="viewSaleDetails(${s.id})">🔍 விவரம்</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Failed to load sales history:", e);
    }
}

document.getElementById('refreshSalesBtn')?.addEventListener('click', loadSalesHistory);

async function viewSaleDetails(saleId) {
    try {
        const sale = await api.get(`/api/sales/${saleId}`);
        alert(`பில் எண்: ${sale.saleNumber}\nமொத்த தொகை: ₹${sale.totalAmount.toFixed(2)}\nபொருட்கள்:\n` +
            sale.items.map(it => `• ${it.productNameTa || it.productNameEn}: ${it.quantity} ${it.unitCode} × ₹${it.unitPriceSnapshot} = ₹${it.subtotal}`).join('\n')
        );
    } catch (e) {
        showAlert("Failed to load sale: " + e.message, "error");
    }
}

// ==========================================
// 12. TAB NAVIGATION & UTILITIES
// ==========================================
function switchToTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabId);
    });
    document.querySelectorAll('.tab-content').forEach(section => {
        section.classList.toggle('active', section.id === tabId);
    });
    state.activeTab = tabId;

    if (tabId === 'priceHistoryTab') loadPriceHistory();
    if (tabId === 'salesHistoryTab') loadSalesHistory();
}

document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const tabId = btn.dataset.tab;
        switchToTab(tabId);
    });
});

// Role Switcher
document.getElementById('authSwitchBtn')?.addEventListener('click', () => {
    if (state.currentRole === 'SHOPKEEPER') {
        state.currentRole = 'ADMIN';
        state.currentUsername = 'admin';
        document.getElementById('userRoleText').textContent = "நிர்வாகி (Admin)";
        document.getElementById('authSwitchBtn').textContent = "கடைக்காரர் (Shopkeeper)";
        document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'flex');
        showAlert("Switched to ADMIN role. All management features unlocked.", "success");
    } else {
        state.currentRole = 'SHOPKEEPER';
        state.currentUsername = 'shopkeeper';
        document.getElementById('userRoleText').textContent = "கடைக்காரர் (Shopkeeper)";
        document.getElementById('authSwitchBtn').textContent = "நிர்வாகி (Admin)";
        document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
        showAlert("Switched to SHOPKEEPER role. Simple touch mode enabled.", "success");
        if (state.activeTab !== 'sellTab' && state.activeTab !== 'priceLookupTab' && state.activeTab !== 'salesHistoryTab') {
            switchToTab('sellTab');
        }
    }
});

// Repeat audio buttons
document.getElementById('repeatAudioBtn')?.addEventListener('click', () => voiceService.repeatLast());
document.getElementById('bannerRepeatBtn')?.addEventListener('click', () => voiceService.repeatLast());

// Language Toggle
document.getElementById('langToggleBtn')?.addEventListener('click', () => {
    state.language = (state.language === 'ta') ? 'en' : 'ta';
    try {
        localStorage.setItem('grocery_lang', state.language);
    } catch (e) {}
    applyLanguageTranslations();
    const switchMsg = (state.language === 'en') ? "Language changed to English" : "மொழி தமிழுக்கு மாற்றப்பட்டது";
    showAlert(switchMsg, "info");
});

function applyLanguageTranslations() {
    const isEn = (state.language === 'en');

    // Language Toggle button indicator
    const langDisplay = document.getElementById('langDisplay');
    if (langDisplay) {
        langDisplay.innerHTML = isEn ? '🌐 <strong>English</strong> | தமிழ்' : '🌐 <strong>தமிழ்</strong> | English';
    }

    // Header
    const appTitle = document.getElementById('appTitle');
    if (appTitle) appTitle.textContent = isEn ? "Smart Grocery Assistant" : "ஸ்மார்ட் மளிகை உதவியாளர்";

    const appSubtitle = document.getElementById('appSubtitle');
    if (appSubtitle) appSubtitle.textContent = isEn ? "Smart Grocery Shop Management" : "Smart Grocery Assistant";

    const pwaInstallLabel = document.getElementById('pwaInstallLabel');
    if (pwaInstallLabel) pwaInstallLabel.textContent = isEn ? "Install App" : "இன்ஸ்டால் (Install)";

    const repeatAudioLabel = document.querySelector('#repeatAudioBtn .audio-btn-label');
    if (repeatAudioLabel) repeatAudioLabel.textContent = isEn ? "Repeat Audio" : "திரும்பக் கேள்";

    const bannerRepeatBtn = document.getElementById('bannerRepeatBtn');
    if (bannerRepeatBtn) bannerRepeatBtn.textContent = isEn ? "🔁 Repeat Voice" : "🔁 திரும்பக் கூறு";

    // Role text
    const userRoleText = document.getElementById('userRoleText');
    if (userRoleText) {
        if (state.currentRole === 'ADMIN') {
            userRoleText.textContent = isEn ? "Administrator" : "நிர்வாகி (Admin)";
        } else {
            userRoleText.textContent = isEn ? "Shopkeeper" : "கடைக்காரர் (Shopkeeper)";
        }
    }

    // Navigation Tabs
    const setTab = (id, primaryTa, secondaryEn) => {
        const btn = document.getElementById(id);
        if (!btn) return;
        const titleEl = btn.querySelector('.tab-title');
        const smallEl = btn.querySelector('small');
        if (titleEl) titleEl.textContent = isEn ? secondaryEn : primaryTa;
        if (smallEl) smallEl.textContent = isEn ? primaryTa : secondaryEn;
    };

    setTab('tabSell', "விற்பனை", "Sell Products");
    setTab('tabLookup', "விலை அறிதல்", "Find Price");
    setTab('tabManagePrices', "விலை மாற்றம்", "Manage Prices");
    setTab('tabVoicePrice', "குரல் வழி மாற்றம்", "Voice Price Update");
    setTab('tabPriceHistory', "விலை வரலாறு", "Price History");
    setTab('tabInventory', "சரக்கு இருப்பு", "Inventory");
    setTab('tabSalesHistory', "விற்பனை வரலாறு", "Sales History");

    // Sell Tab
    const billSearch = document.getElementById('billProductSearch');
    if (billSearch) billSearch.placeholder = isEn ? "Search product / Barcode..." : "பொருளைத் தேடுக (Search product / Barcode)...";

    const barcodeBtn = document.getElementById('billBarcodeBtn');
    if (barcodeBtn) barcodeBtn.innerHTML = isEn ? "📷 Barcode" : "📷 பார்-கோட்";

    const cartHeading = document.querySelector('.cart-header h2');
    if (cartHeading) cartHeading.textContent = isEn ? "Current Bill" : "தற்போதைய பில் (Current Bill)";

    const clearBtn = document.getElementById('clearBillBtn');
    if (clearBtn) clearBtn.textContent = isEn ? "Clear" : "அழி (Clear)";

    const emptyMsgP = document.querySelector('#emptyCartMsg p');
    if (emptyMsgP) emptyMsgP.textContent = isEn ? "Select items from catalog" : "பொருட்களைத் தேர்வு செய்யவும்";

    const emptyMsgSmall = document.querySelector('#emptyCartMsg small');
    if (emptyMsgSmall) emptyMsgSmall.textContent = isEn ? "Click any card to add" : "No items added yet";

    const completeSaleBtnText = document.querySelector('#completeSaleBtn .btn-text');
    if (completeSaleBtnText) completeSaleBtnText.textContent = isEn ? "Complete Sale" : "பில் முடி (Complete Sale)";

    // Price Lookup Tab
    const lookupTitle = document.querySelector('.lookup-search-card h2');
    if (lookupTitle) lookupTitle.textContent = isEn ? "Find Product Price" : "பொருள் விலை அறிதல் (Find Product Price)";

    const lookupDesc = document.querySelector('.lookup-search-card .section-desc');
    if (lookupDesc) {
        lookupDesc.textContent = isEn 
            ? "Tap an item or search by name / barcode. English & Tamil voice announcements supported."
            : "பொருளைத் தட்டவும் அல்லது பெயர்/பார்கோடு மூலம் தேடவும். தமிழ் குரல் அறிவிப்பு கிடைக்கும்.";
    }

    const lookupInput = document.getElementById('lookupSearchInput');
    if (lookupInput) lookupInput.placeholder = isEn ? "Search by Name / Barcode..." : "தேடவும் (Search by Name / Barcode)...";

    const lookupSpeakBtn = document.getElementById('lookupSpeakBtn');
    if (lookupSpeakBtn) lookupSpeakBtn.textContent = isEn ? "🔊 Hear Price" : "🔊 குரல் கேள் (Hear Price)";

    const lookupAddBtn = document.getElementById('lookupAddToBillBtn');
    if (lookupAddBtn) lookupAddBtn.textContent = isEn ? "➕ Add to Bill" : "➕ பில்லில் சேர் (Add to Bill)";

    // Manage Prices Tab
    const manageTitle = document.querySelector('#managePricesTab .card-box h2');
    if (manageTitle) manageTitle.textContent = isEn ? "Manage Selling Prices" : "நிர்வாக விலை மாற்றம் (Manage Selling Prices)";

    const saveManualBtn = document.getElementById('saveManualPriceBtn');
    if (saveManualBtn) saveManualBtn.textContent = isEn ? "💾 Update Price" : "💾 விலையைச் சேமி (Update Price)";

    // Voice Update Tab
    const voiceTitle = document.querySelector('.voice-card h2');
    if (voiceTitle) voiceTitle.textContent = isEn ? "Voice-Based Price Update" : "குரல் வழி விலை மாற்றம் (Voice-Based Price Update)";

    const voiceStatus = document.getElementById('voiceMicStatusText');
    if (voiceStatus && !speechService?.isListening) {
        voiceStatus.textContent = isEn ? "Tap to Speak" : "பேச தட்டவும் (Tap to Speak)";
    }

    const parseBtn = document.getElementById('parseVoiceCommandManualBtn');
    if (parseBtn) parseBtn.textContent = isEn ? "Parse" : "ஆய்வு செய் (Parse)";

    const confirmVoiceBtn = document.getElementById('confirmVoiceUpdateBtn');
    if (confirmVoiceBtn) confirmVoiceBtn.textContent = isEn ? "✅ Confirm & Update" : "✅ உறுதி செய் (Confirm & Update)";

    const cancelVoiceBtn = document.getElementById('cancelVoiceUpdateBtn');
    if (cancelVoiceBtn) cancelVoiceBtn.textContent = isEn ? "❌ Cancel" : "❌ ரத்து செய் (Cancel)";

    // Keypad Modal Buttons
    const addKeypadBtn = document.getElementById('addKeypadItemBtn');
    if (addKeypadBtn) addKeypadBtn.textContent = isEn ? "Add to Bill" : "பில்லில் சேர் (Add to Bill)";

    const closeKeypadBtn = document.getElementById('closeKeypadBtn');
    if (closeKeypadBtn) closeKeypadBtn.textContent = isEn ? "Cancel" : "ரத்து (Cancel)";

    // Re-render grids and cart to reflect language choice
    if (state.products && state.products.length > 0) {
        renderBillProductGrid(state.products);
        renderVisualLookupGrid(state.products);
    }
    renderCart();
}

function showAlert(message, type = 'info') {
    const banner = document.getElementById('systemNotification');
    if (!banner) return;
    banner.className = `alert-banner ${type}`;
    banner.textContent = message;
    banner.style.display = 'block';
    setTimeout(() => {
        banner.style.display = 'none';
    }, 4500);
}

// Format integer/decimal rupees to Tamil words for UI display
function formatRupeesInTamil(amount) {
    if (!amount || amount === 0) return "பூஜ்ஜியம் ரூபாய்";
    const rounded = Math.round(amount);
    return `சுமார் ${rounded} ரூபாய்`;
}

// Format integer/decimal rupees to English words for UI display
function formatRupeesInEnglish(amount) {
    if (!amount || amount === 0) return "Zero Rupees";
    const rounded = Math.round(amount);
    const ones = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine',
        'Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];
    const tens = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

    function convertBelowThousand(n) {
        if (n === 0) return '';
        if (n < 20) return ones[n];
        if (n < 100) return tens[Math.floor(n / 10)] + (n % 10 !== 0 ? ' ' + ones[n % 10] : '');
        return ones[Math.floor(n / 100)] + ' Hundred' + (n % 100 !== 0 ? ' ' + convertBelowThousand(n % 100) : '');
    }

    let result = '';
    let n = rounded;
    if (n >= 10000000) {
        result += convertBelowThousand(Math.floor(n / 10000000)) + ' Crore ';
        n %= 10000000;
    }
    if (n >= 100000) {
        result += convertBelowThousand(Math.floor(n / 100000)) + ' Lakh ';
        n %= 100000;
    }
    if (n >= 1000) {
        result += convertBelowThousand(Math.floor(n / 1000)) + ' Thousand ';
        n %= 1000;
    }
    if (n > 0) {
        result += convertBelowThousand(n);
    }
    return (result.trim() || 'Zero') + ' Rupees';
}

// PWA Service Worker Registration & Mobile Install Prompt
let deferredInstallPrompt = null;

if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(reg => console.log('PWA ServiceWorker registered successfully:', reg.scope))
            .catch(err => console.log('ServiceWorker registration skipped:', err));
    });
}

window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredInstallPrompt = e;
    const installBtn = document.getElementById('pwaInstallBtn');
    if (installBtn) {
        installBtn.style.display = 'inline-flex';
        installBtn.onclick = async () => {
            if (deferredInstallPrompt) {
                deferredInstallPrompt.prompt();
                const choice = await deferredInstallPrompt.userChoice;
                if (choice.outcome === 'accepted') {
                    showAlert(state.language === 'en' ? "App installed on your device!" : "செயலி உங்கள் போனில் நிறுவப்பட்டது! (App installed!)", "success");
                }
                deferredInstallPrompt = null;
                installBtn.style.display = 'none';
            }
        };
    }
});

// Initial bootstrap
window.addEventListener('DOMContentLoaded', () => {
    applyLanguageTranslations();
    loadProducts();
});


