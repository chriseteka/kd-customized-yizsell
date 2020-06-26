package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/26/2020
 * @email chriseteka@gmail.com
 */
@Data
public class WarehouseStocks implements Serializable {

    private Long warehouseStockId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private StockCategory stockCategory;
    private String stockName;
    private int stockQuantityPurchased;
    private int stockQuantitySold;
    private int stockQuantityRemaining;
    private BigDecimal stockPurchasedTotalPrice;
    private BigDecimal pricePerStockPurchased;
    private BigDecimal stockSoldTotalPrice;
    private BigDecimal stockRemainingTotalPrice;
    private BigDecimal sellingPricePerStock;
    private int lastRestockQuantity;
    private BigDecimal profit;
    private String createdBy;
    private String lastRestockBy;
    private Boolean approved;
    private Date approvedDate;
    private String approvedBy;
    private Date expiryDate;
    private String stockBarCodeId;
    private int possibleQuantityRemaining;
    private Warehouse warehouse;

    public WarehouseStocks(Long warehouseStockId, Date createdDate, Date createdTime, Date updateDate,
                       StockCategory stockCategory, String stockName, int stockQuantityPurchased, int stockQuantitySold,
                       int stockQuantityRemaining, BigDecimal stockPurchasedTotalPrice, BigDecimal pricePerStockPurchased,
                       BigDecimal stockSoldTotalPrice, BigDecimal stockRemainingTotalPrice, BigDecimal sellingPricePerStock,
                       int lastRestockQuantity, BigDecimal profit, String createdBy, String lastRestockBy, Boolean approved,
                       Date approvedDate, String approvedBy, Date expiryDate, String stockBarCodeId,
                       int possibleQuantityRemaining, Warehouse warehouse) {
        this.warehouseStockId = warehouseStockId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.stockCategory = stockCategory;
        this.stockName = stockName;
        this.stockQuantityPurchased = stockQuantityPurchased;
        this.stockQuantitySold = stockQuantitySold;
        this.stockQuantityRemaining = stockQuantityRemaining;
        this.stockPurchasedTotalPrice = stockPurchasedTotalPrice;
        this.pricePerStockPurchased = pricePerStockPurchased;
        this.stockSoldTotalPrice = stockSoldTotalPrice;
        this.stockRemainingTotalPrice = stockRemainingTotalPrice;
        this.sellingPricePerStock = sellingPricePerStock;
        this.lastRestockQuantity = lastRestockQuantity;
        this.profit = profit;
        this.createdBy = createdBy;
        this.lastRestockBy = lastRestockBy;
        this.approved = approved;
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.expiryDate = expiryDate;
        this.stockBarCodeId = stockBarCodeId;
        this.possibleQuantityRemaining = possibleQuantityRemaining;
        this.warehouse = warehouse;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks fromDTO(){
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks warehouseStocksFromDTO =
                new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.WarehouseStocks();
        warehouseStocksFromDTO.setWarehouseStockId(this.getWarehouseStockId());
        warehouseStocksFromDTO.setCreatedDate(this.getCreatedDate());
        warehouseStocksFromDTO.setCreatedTime(this.getCreatedTime());
        warehouseStocksFromDTO.setUpdateDate(this.getUpdateDate());
        warehouseStocksFromDTO.setStockCategory(this.getStockCategory().fromDTO());
        warehouseStocksFromDTO.setStockName(this.getStockName());
        warehouseStocksFromDTO.setStockQuantityPurchased(this.getStockQuantityPurchased());
        warehouseStocksFromDTO.setStockQuantitySold(this.getStockQuantitySold());
        warehouseStocksFromDTO.setStockQuantityRemaining(this.getStockQuantityRemaining());
        warehouseStocksFromDTO.setStockPurchasedTotalPrice(this.getStockPurchasedTotalPrice());
        warehouseStocksFromDTO.setPricePerStockPurchased(this.getPricePerStockPurchased());
        warehouseStocksFromDTO.setStockSoldTotalPrice(this.getStockSoldTotalPrice());
        warehouseStocksFromDTO.setStockRemainingTotalPrice(this.getStockRemainingTotalPrice());
        warehouseStocksFromDTO.setSellingPricePerStock(this.getSellingPricePerStock());
        warehouseStocksFromDTO.setLastRestockQuantity(this.getLastRestockQuantity());
        warehouseStocksFromDTO.setProfit(this.getProfit());
        warehouseStocksFromDTO.setCreatedBy(this.getCreatedBy());
        warehouseStocksFromDTO.setLastRestockBy(this.getLastRestockBy());
        warehouseStocksFromDTO.setApproved(this.getApproved());
        warehouseStocksFromDTO.setApprovedDate(this.getApprovedDate());
        warehouseStocksFromDTO.setApprovedBy(this.getApprovedBy());
        warehouseStocksFromDTO.setExpiryDate(this.getExpiryDate());
        warehouseStocksFromDTO.setStockBarCodeId(this.getStockBarCodeId());
        warehouseStocksFromDTO.setPossibleQuantityRemaining(this.getPossibleQuantityRemaining());
        warehouseStocksFromDTO.setWarehouse(this.getWarehouse().fromDTO());

        return warehouseStocksFromDTO;
    }
}
