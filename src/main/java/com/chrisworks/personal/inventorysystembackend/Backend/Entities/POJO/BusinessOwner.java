package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIME;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "business-owners")
public class BusinessOwner {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long businessOwnerId;

    @Temporal(DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Business name cannot be null")
    @Size(min = 3, message = "Business Name must be at least three characters")
    @Column(name = "business-name", nullable = false)
    private String businessName;

    @NotNull(message = "Business owner full name cannot be null")
    @Size(min = 3, message = "Business Owner Full Name must be at least three characters")
    @Column(name = "business-owner-full-name", nullable = false)
    private String businessOwnerFullName;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid Email Address Entered", regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")
    @Column(name = "business-owner-email", unique = true, nullable = false)
    private String businessOwnerEmail;

    @NotNull(message = "Business owner phone number cannot be null")
    @Pattern(regexp = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}", message = "Invalid Phone Number Entered")
    @Column(name = "business-owner-phone-number", nullable = false)
    private String businessOwnerPhoneNumber;

    @NotNull(message = "Business owner password cannot be null")
    @Size(min = 4, message = "Business Owner Full Name must be at least four characters")
    @Column(name = "business-owner-password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String businessOwnerPassword;

    @Column(name = "business-total-income", precision = 2)
    private BigDecimal businessTotalIncome;

    @Column(name = "business-total-expenses", precision = 2)
    private BigDecimal businessTotalExpenses;

    @Column(name = "business-total-profit", precision = 2)
    private BigDecimal businessTotalProfit;

    @ManyToMany
    @JoinTable(name = "business-owner-warehouses", joinColumns = @JoinColumn(name = "businessOwnerId"), inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    private Set<Warehouse> warehouses = new HashSet<>();

}
