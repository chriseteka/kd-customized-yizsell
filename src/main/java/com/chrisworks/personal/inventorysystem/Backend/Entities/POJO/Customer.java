package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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

    @Size(min = 3, message = "Customer name must have at least three characters")
    @Column(name = "customerFullName", nullable = false)
    private String customerFullName;

    @Size(min = 5, max = 15, message = "Invalid Phone Number Entered")
    @Column(name = "customerPhoneNumber", nullable = false)
    private String customerPhoneNumber;

    @Email(message = "Invalid Email Address Entered")
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
}
