package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/26/2020
 * @email chriseteka@gmail.com
 */
@Data
public class StockCategory {

    private Long StockCategoryId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String categoryName;
    private String createdBy;

    public StockCategory(Long stockCategoryId, Date createdDate, Date createdTime, Date updateDate, String categoryName,
                     String createdBy) {
        StockCategoryId = stockCategoryId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.categoryName = categoryName;
        this.createdBy = createdBy;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory fromDTO(){

        return new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory(this.getStockCategoryId(),
            this.getCreatedDate(), this.getCreatedTime(), this.getUpdateDate(), this.getCategoryName(), this.getCreatedBy());
    }
}
