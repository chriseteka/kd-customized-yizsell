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
public class Customer implements Serializable {

    private Long customerId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private String customerFullName;
    private String customerPhoneNumber;
    private String customerEmail;
    private Boolean isLoyal;
    private int numberOfPurchasesAfterLastReward;
    private BigDecimal recentPurchasesAmount;
    private String createdBy;
    private int numberOfPurchaseTimesBeforeReward;
    private BigDecimal threshold;
    private BigDecimal debt;

    public Customer(Long customerId, Date createdDate, Date createdTime, Date updateDate, String customerFullName,
                String customerPhoneNumber, String customerEmail, Boolean isLoyal, int numberOfPurchasesAfterLastReward,
                BigDecimal recentPurchasesAmount, String createdBy, int numberOfPurchaseTimesBeforeReward,
                BigDecimal threshold, BigDecimal debt) {
        this.customerId = customerId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.customerFullName = customerFullName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerEmail = customerEmail;
        this.isLoyal = isLoyal;
        this.numberOfPurchasesAfterLastReward = numberOfPurchasesAfterLastReward;
        this.recentPurchasesAmount = recentPurchasesAmount;
        this.createdBy = createdBy;
        this.numberOfPurchaseTimesBeforeReward = numberOfPurchaseTimesBeforeReward;
        this.threshold = threshold;
        this.debt = debt;
    }

    com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer fromDTO() {
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer customerFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer();
        customerFromDTO.setCustomerId(this.getCustomerId());
        customerFromDTO.setCreatedDate(this.getCreatedDate());
        customerFromDTO.setCreatedTime(this.getCreatedTime());
        customerFromDTO.setUpdateDate(this.getUpdateDate());
        customerFromDTO.setCustomerFullName(this.getCustomerFullName());
        customerFromDTO.setCustomerPhoneNumber(this.getCustomerPhoneNumber());
        customerFromDTO.setCustomerEmail(this.getCustomerEmail());
        customerFromDTO.setIsLoyal(this.isLoyal);
        customerFromDTO.setNumberOfPurchasesAfterLastReward(this.getNumberOfPurchasesAfterLastReward());
        customerFromDTO.setRecentPurchasesAmount(this.getRecentPurchasesAmount());
        customerFromDTO.setCreatedBy(this.getCreatedBy());
        customerFromDTO.setNumberOfPurchaseTimesBeforeReward(this.getNumberOfPurchaseTimesBeforeReward());
        customerFromDTO.setThreshold(this.getThreshold());
        customerFromDTO.setDebt(this.getDebt());

        return customerFromDTO;
    }
}
