package com.inventorymanagement.backend.interfaces;

import com.inventorymanagement.backend.dto.InventoryItemDto;

import java.util.List;

public interface InventoryService {

    InventoryItemDto createItem(InventoryItemDto inventoryItemDto);

    InventoryItemDto updateItem(Long id, InventoryItemDto dto);

    void deleteItem(Long id);

    InventoryItemDto getItem(Long id);

    List<InventoryItemDto> getAllItems();

}
