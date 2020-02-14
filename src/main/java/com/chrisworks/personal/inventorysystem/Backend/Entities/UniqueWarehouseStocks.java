package com.chrisworks.personal.inventorysystem.Backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniqueWarehouseStocks {

    private Long stockId;

    private String stockName;

    private int quantityRemaining;

    private BigDecimal currentCostPrice;

    private BigDecimal currentSellingPrice;
}
