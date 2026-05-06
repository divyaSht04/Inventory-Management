package com.inventorymanagement.backend.service;

import com.inventorymanagement.backend.dto.InventoryItemDto;
import com.inventorymanagement.backend.entity.InventoryItem;
import com.inventorymanagement.backend.exception.InventoryConflictException;
import com.inventorymanagement.backend.exception.InventoryNotFoundException;
import com.inventorymanagement.backend.interfaces.InventoryService;
import com.inventorymanagement.backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryItemDto createItem(InventoryItemDto inventoryItemDto) {
        InventoryItem item = mapToEntity(inventoryItemDto);
        item = inventoryRepository.save(item);
        return mapToDto(item);
    }

    @Transactional
    public InventoryItemDto updateItem(Long id, InventoryItemDto dto) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Item not found: " + id));

        item.setSku(dto.getSku());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());

        try {
            item = inventoryRepository.save(item);
        } catch (OptimisticLockingFailureException ex) {
            throw new InventoryConflictException("Item was modified by another user. Please reload and try again.");
        }
        return mapToDto(item);
    }

    @Transactional
    public InventoryItemDto getItem(Long id) {
        return inventoryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new InventoryNotFoundException("Item not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new InventoryNotFoundException("Item not found: " + id);
        }
        inventoryRepository.deleteById(id);
    }

    private InventoryItem mapToEntity(InventoryItemDto dto) {
        return InventoryItem.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build();
    }

    private InventoryItemDto mapToDto(InventoryItem item) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(item.getId());
        dto.setSku(item.getSku());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
