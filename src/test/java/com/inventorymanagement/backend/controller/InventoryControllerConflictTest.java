package com.inventorymanagement.backend.controller;

import com.inventorymanagement.backend.exception.InventoryConflictException;
import com.inventorymanagement.backend.interfaces.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test — only the web layer.
 * Verifies InventoryConflictException is mapped to HTTP 409 with ProblemDetail body.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerConflictTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean InventoryService inventoryService;

    private static final String VALID_BODY = """
            {
                "sku": "SKU-001",
                "name": "Widget",
                "description": "A widget",
                "quantity": 5,
                "price": 14.99
            }
            """;

    @Test
    @WithMockUser
    void update_returns409_withProblemDetail_whenConflict() throws Exception {
        when(inventoryService.updateItem(eq(1L), any()))
                .thenThrow(new InventoryConflictException(
                        "Item was modified by another user. Please reload and try again."));

        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Item was modified by another user. Please reload and try again."));
    }
}
