package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/24/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Seller implements Serializable {

    private Long sellerId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String sellerFullName;
    private String sellerEmail;
    private String sellerPhoneNumber;
    private String sellerAddress;

    public Seller(Long sellerId, Date createdDate, Date createdTime, Date updateDate, String sellerFullName,
              String sellerEmail, String sellerPhoneNumber, String sellerAddress) {
        this.sellerId = sellerId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.sellerFullName = sellerFullName;
        this.sellerEmail = sellerEmail;
        this.sellerPhoneNumber = sellerPhoneNumber;
        this.sellerAddress = sellerAddress;
    }

    com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller fromDTO() {
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller sellerFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller();
        sellerFromDTO.setSellerId(this.getSellerId());
        sellerFromDTO.setCreatedDate(this.getCreatedDate());
        sellerFromDTO.setCreatedTime(this.getCreatedTime());
        sellerFromDTO.setUpdateDate(this.getUpdateDate());
        sellerFromDTO.setSellerFullName(this.getSellerFullName());
        sellerFromDTO.setSellerEmail(this.getSellerEmail());
        sellerFromDTO.setSellerPhoneNumber(this.getSellerPhoneNumber());
        sellerFromDTO.setSellerAddress(this.getSellerAddress());

        return sellerFromDTO;
    }
}
