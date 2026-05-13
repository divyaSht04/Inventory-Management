package com.inventorymanagement.backend.controller;

import com.inventorymanagement.backend.dto.InventoryItemDto;
import com.inventorymanagement.backend.interfaces.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemDto> create(@Valid @RequestBody InventoryItemDto dto) {
        InventoryItemDto created = inventoryService.createItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody InventoryItemDto dto) {
        InventoryItemDto updated = inventoryService.updateItem(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItem(id));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAll() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}