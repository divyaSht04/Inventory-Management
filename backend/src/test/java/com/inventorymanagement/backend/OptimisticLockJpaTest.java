package com.inventorymanagement.backend;

import com.inventorymanagement.backend.dto.InventoryItemDto;
import com.inventorymanagement.backend.entity.InventoryItem;
import com.inventorymanagement.backend.interfaces.InventoryService;
import com.inventorymanagement.backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptimisticLockJpaTest {

    @Autowired InventoryService inventoryService;
    @Autowired InventoryRepository inventoryRepository;

    private Long itemId;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();

        InventoryItemDto dto = new InventoryItemDto();
        dto.setSku("LOCK-001");
        dto.setName("Test Widget");
        dto.setDescription("Widget for optimistic lock tests");
        dto.setQuantity(10);
        dto.setPrice(new BigDecimal("9.99"));

        itemId = inventoryService.createItem(dto).getId();
    }

    @Test
    void version_startsAtZero_andIncrementsAfterUpdate() {
        InventoryItem fresh = inventoryRepository.findById(itemId).get();
        assertThat(fresh.getVersion()).isEqualTo(0L);

        inventoryService.updateItem(itemId, buildDto("LOCK-001", "Updated", "desc", 5, "14.99"));

        InventoryItem afterUpdate = inventoryRepository.findById(itemId).get();
        assertThat(afterUpdate.getVersion()).isEqualTo(1L);
    }


    @Test
    void savingStaleSnapshot_throwsOptimisticLockingFailureException() {
        // User A reads the entity — transaction closes, entity is now detached (version = 0)
        InventoryItem userASnapshot = inventoryRepository.findById(itemId).get();

        // User B updates successfully — DB version is now 1
        inventoryService.updateItem(itemId, buildDto("LOCK-001", "User B update", "B was first", 20, "19.99"));

        // User A tries to persist their stale snapshot (version 0 vs DB version 1)
        // Hibernate generates: UPDATE inventory_item SET ... WHERE id=? AND version=0
        // That WHERE matches 0 rows → OptimisticLockingFailureException
        userASnapshot.setName("User A stale update");

        assertThatThrownBy(() -> inventoryRepository.saveAndFlush(userASnapshot))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    // --- helper ---

    private InventoryItemDto buildDto(String sku, String name, String desc, int qty, String price) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setSku(sku);
        dto.setName(name);
        dto.setDescription(desc);
        dto.setQuantity(qty);
        dto.setPrice(new BigDecimal(price));
        return dto;
    }
}
