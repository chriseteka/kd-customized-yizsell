package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/24/2020
 * @email chriseteka@gmail.com
 */
@Data
public class StockSold implements Serializable {

    private Long stockSoldId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String stockName;
    private String stockCategory;
    private int quantitySold;
    private BigDecimal pricePerStockSold;
    private BigDecimal costPricePerStock;
    private String stockSoldInvoiceId;
    private String createdBy;
    private boolean soldOnPromo;
    private int quantitySoldOnPromo;
    private boolean promoApplied;

    public StockSold(Long stockSoldId, Date createdDate, Date createdTime, Date updateDate, String stockName,
                 String stockCategory, int quantitySold, BigDecimal pricePerStockSold, BigDecimal costPricePerStock,
                 String stockSoldInvoiceId, String createdBy, boolean soldOnPromo, int quantitySoldOnPromo,
                 boolean promoApplied) {
        this.stockSoldId = stockSoldId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.stockName = stockName;
        this.stockCategory = stockCategory;
        this.quantitySold = quantitySold;
        this.pricePerStockSold = pricePerStockSold;
        this.costPricePerStock = costPricePerStock;
        this.stockSoldInvoiceId = stockSoldInvoiceId;
        this.createdBy = createdBy;
        this.soldOnPromo = soldOnPromo;
        this.quantitySoldOnPromo = quantitySoldOnPromo;
        this.promoApplied = promoApplied;
    }

    com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold fromDTO() {

        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold stockSoldFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold();
        stockSoldFromDTO.setStockSoldId(this.getStockSoldId());
        stockSoldFromDTO.setCreatedDate(this.getCreatedDate());
        stockSoldFromDTO.setCreatedTime(this.getCreatedTime());
        stockSoldFromDTO.setUpdateDate(this.getUpdateDate());
        stockSoldFromDTO.setStockName(this.getStockName());
        stockSoldFromDTO.setStockCategory(this.getStockCategory());
        stockSoldFromDTO.setQuantitySold(this.getQuantitySold());
        stockSoldFromDTO.setPricePerStockSold(this.getPricePerStockSold());
        stockSoldFromDTO.setCostPricePerStock(this.getCostPricePerStock());
        stockSoldFromDTO.setStockSoldInvoiceId(this.getStockSoldInvoiceId());
        stockSoldFromDTO.setCreatedBy(this.getCreatedBy());
        stockSoldFromDTO.setSoldOnPromo(this.isSoldOnPromo());
        stockSoldFromDTO.setQuantitySoldOnPromo(this.getQuantitySoldOnPromo());
        stockSoldFromDTO.setPromoApplied(this.isPromoApplied());

        return stockSoldFromDTO;
    }
}
