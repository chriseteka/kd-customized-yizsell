package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/25/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Shop implements Serializable {

    private Long shopId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String shopName;
    private String shopAddress;
    private String createdBy;

    public Shop(Long shopId, Date createdDate, Date createdTime, Date updateDate, String shopName, String shopAddress,
            String createdBy) {
        this.shopId = shopId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.createdBy = createdBy;
    }

    com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop fromDTO() {
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop shopFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop();
        shopFromDTO.setShopId(this.getShopId());
        shopFromDTO.setCreatedDate(this.getCreatedDate());
        shopFromDTO.setCreatedTime(this.getCreatedTime());
        shopFromDTO.setUpdateDate(this.getUpdateDate());
        shopFromDTO.setShopName(this.getShopName());
        shopFromDTO.setShopAddress(this.getShopAddress());
        shopFromDTO.setCreatedBy(this.getCreatedBy());

        return shopFromDTO;
    }
}
