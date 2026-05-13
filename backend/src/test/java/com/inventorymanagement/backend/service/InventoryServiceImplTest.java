package com.inventorymanagement.backend.service;

import com.inventorymanagement.backend.dto.InventoryItemDto;
import com.inventorymanagement.backend.entity.InventoryItem;
import com.inventorymanagement.backend.exception.InventoryConflictException;
import com.inventorymanagement.backend.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test — no Spring context.
 * Verifies the service converts OptimisticLockingFailureException → InventoryConflictException.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock  InventoryRepository inventoryRepository;
    @InjectMocks InventoryServiceImpl inventoryService;

    @Test
    void updateItem_wrapsJpaConflict_asInventoryConflictException() {
        InventoryItem existing = InventoryItem.builder()
                .sku("SKU-001")
                .name("Widget")
                .description("A widget")
                .quantity(10)
                .price(new BigDecimal("9.99"))
                .build();

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        // Simulate another transaction having committed between the service's load and save
        when(inventoryRepository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("Row was updated by another transaction"));

        InventoryItemDto dto = new InventoryItemDto();
        dto.setSku("SKU-001");
        dto.setName("My update");
        dto.setDescription("My desc");
        dto.setQuantity(5);
        dto.setPrice(new BigDecimal("14.99"));

        assertThatThrownBy(() -> inventoryService.updateItem(1L, dto))
                .isInstanceOf(InventoryConflictException.class)
                .hasMessageContaining("modified by another user");
    }
}
