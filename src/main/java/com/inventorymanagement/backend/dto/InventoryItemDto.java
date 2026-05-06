package com.inventorymanagement.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryItemDto {

    private long id;

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Min(0)
    private int quantity;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

}