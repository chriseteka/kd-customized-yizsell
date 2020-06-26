package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/26/2020
 * @email chriseteka@gmail.com
 */
@Data
public class ShopStocks {

    private Long shopStockId;
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
    private boolean hasPromo;
    private Shop shop;

    public ShopStocks(Long shopStockId, Date createdDate, Date createdTime, Date updateDate, StockCategory stockCategory,
                  String stockName, int stockQuantityPurchased, int stockQuantitySold, int stockQuantityRemaining,
                  BigDecimal stockPurchasedTotalPrice, BigDecimal pricePerStockPurchased,
                  BigDecimal stockSoldTotalPrice, BigDecimal stockRemainingTotalPrice, BigDecimal sellingPricePerStock,
                  int lastRestockQuantity, BigDecimal profit, String createdBy, String lastRestockBy, Boolean approved,
                  Date approvedDate, String approvedBy, Date expiryDate, String stockBarCodeId, boolean hasPromo,
                  Shop shop) {
        this.shopStockId = shopStockId;
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
        this.hasPromo = hasPromo;
        this.shop = shop;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks fromDTO(){
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks shopStocksFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks();
        shopStocksFromDTO.setShopStockId(this.getShopStockId());
        shopStocksFromDTO.setCreatedDate(this.getCreatedDate());
        shopStocksFromDTO.setCreatedTime(this.getCreatedTime());
        shopStocksFromDTO.setUpdateDate(this.getUpdateDate());
        shopStocksFromDTO.setStockCategory(this.getStockCategory().fromDTO());
        shopStocksFromDTO.setStockName(this.getStockName());
        shopStocksFromDTO.setStockQuantityPurchased(this.getStockQuantityPurchased());
        shopStocksFromDTO.setStockQuantitySold(this.getStockQuantitySold());
        shopStocksFromDTO.setStockQuantityRemaining(this.getStockQuantityRemaining());
        shopStocksFromDTO.setStockPurchasedTotalPrice(this.getStockPurchasedTotalPrice());
        shopStocksFromDTO.setPricePerStockPurchased(this.getPricePerStockPurchased());
        shopStocksFromDTO.setStockSoldTotalPrice(this.getStockSoldTotalPrice());
        shopStocksFromDTO.setStockRemainingTotalPrice(this.getStockRemainingTotalPrice());
        shopStocksFromDTO.setSellingPricePerStock(this.getSellingPricePerStock());
        shopStocksFromDTO.setLastRestockQuantity(this.getLastRestockQuantity());
        shopStocksFromDTO.setProfit(this.getProfit());
        shopStocksFromDTO.setCreatedBy(this.getCreatedBy());
        shopStocksFromDTO.setLastRestockBy(this.getLastRestockBy());
        shopStocksFromDTO.setApproved(this.getApproved());
        shopStocksFromDTO.setApprovedDate(this.getApprovedDate());
        shopStocksFromDTO.setApprovedBy(this.getApprovedBy());
        shopStocksFromDTO.setExpiryDate(this.getExpiryDate());
        shopStocksFromDTO.setStockBarCodeId(this.getStockBarCodeId());
        shopStocksFromDTO.setHasPromo(this.isHasPromo());
        shopStocksFromDTO.setShop(this.getShop().fromDTO());

        return shopStocksFromDTO;
    }
}
