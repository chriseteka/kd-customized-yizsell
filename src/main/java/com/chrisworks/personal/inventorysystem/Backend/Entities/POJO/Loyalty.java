package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Loyalties")
public class Loyalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long LoyaltyId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    @NotEmpty(message = "loyalty name cannot be empty")
    private String loyaltyName;

    @Min(value = 1, message = "Number of days before reward must be at least 1")
    private int numberOfDaysBeforeReward;

    @DecimalMin(value = "0.0", inclusive = false, message = "Threshold must be greater than zero")
    private BigDecimal threshold;

    private String createdBy;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "loyalCustomers", joinColumns = @JoinColumn(name = "loyaltyId",
            nullable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "customerId"))
    private Set<Customer> customers = new HashSet<>();

    @PrePersist
    void setTransients(){
        this.loyaltyName = loyaltyName.toUpperCase();
        this.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }
}
