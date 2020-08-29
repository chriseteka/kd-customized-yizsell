package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long customerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @Column(name = "customerFullName", nullable = false)
    private String customerFullName;

    @Column(name = "customerPhoneNumber", nullable = false)
    private String customerPhoneNumber;

    @Column(name = "customerEmail")
    private String customerEmail;

    @Column(name = "isLoyal")
    private Boolean isLoyal = false;

    @Column(name = "purchasesAfterLastReward")
    private int numberOfPurchasesAfterLastReward = 0;

    @Column(name = "recentPurchasesAmount")
    private BigDecimal recentPurchasesAmount = BigDecimal.ZERO;

    @Column(name = "createdBy")
    private String createdBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "purchaseTimesBeforeNextReward")
    private int numberOfPurchaseTimesBeforeReward = 0;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "threshold")
    private BigDecimal threshold = BigDecimal.ZERO;

    @Transient
    private BigDecimal debt = BigDecimal.ZERO;

    public void setDebt(BigDecimal debt){
        this.debt = debt;
    }

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }
}
