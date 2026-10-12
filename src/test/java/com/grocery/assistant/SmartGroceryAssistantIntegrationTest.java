package com.grocery.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grocery.assistant.dto.PriceUpdateDto;
import com.grocery.assistant.dto.SaleItemRequestDto;
import com.grocery.assistant.dto.SaleRequestDto;
import com.grocery.assistant.dto.VoicePriceParseRequest;
import com.grocery.assistant.entity.enums.Unit;
import com.grocery.assistant.entity.enums.UpdateMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmartGroceryAssistantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Public static home index.html is accessible without authentication")
    void testPublicIndexHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated request to products is rejected with 401 Unauthorized")
    void testUnauthorizedProductAccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "shopkeeper", roles = {"SHOPKEEPER"})
    @DisplayName("Shopkeeper role can search and retrieve active products")
    void testShopkeeperGetProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));
    }

    @Test
    @WithMockUser(username = "shopkeeper", roles = {"SHOPKEEPER"})
    @DisplayName("Shopkeeper role is forbidden (403) from updating product price")
    void testShopkeeperForbiddenFromPriceUpdate() throws Exception {
        PriceUpdateDto dto = new PriceUpdateDto(new BigDecimal("60.00"), UpdateMethod.MANUAL, "Test reason");

        mockMvc.perform(patch("/api/products/1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admin role can parse voice price command safely without modifying DB")
    void testAdminVoiceParseCommand() throws Exception {
        VoicePriceParseRequest req = new VoicePriceParseRequest("Change rice price to 55 rupees per kilogram");

        mockMvc.perform(post("/api/voice/parse-price-command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recognizedText", notNullValue()));
    }

    @Test
    @WithMockUser(username = "shopkeeper", roles = {"SHOPKEEPER"})
    @DisplayName("Empty bill submission is rejected with 400 Bad Request")
    void testEmptyBillRejected() throws Exception {
        SaleRequestDto req = new SaleRequestDto();
        req.setItems(List.of());

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
