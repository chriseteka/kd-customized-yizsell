package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
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
@Table(name = "Income")
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long incomeId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @DecimalMin(value = "0.0", inclusive = false, message = "Income amount must be greater than zero")
    @NotNull(message = "income amount cannot be null")
    @Column(name = "incomeAmount", nullable = false)
    private BigDecimal incomeAmount;

    @Column(name = "incomeReference")
    private String incomeReference = "Nil";

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedDate")
    private Date approvedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinTable(name = "shopIncome", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "incomeId"))
//    private Shop shop;

    @Basic
    @JsonIgnore
    @NotNull(message = "Income type cannot be null")
    @Column(name = "incomeType", nullable = false)
    private int incomeTypeValue;

    @Transient
    private INCOME_TYPE incomeType;

    @PostLoad
    void fillTransient() {
        if (incomeTypeValue > 0) {
            this.incomeType = INCOME_TYPE.of(incomeTypeValue);
        }
    }

    @PrePersist
    void fillPersistent() {
        if (incomeType != null) {
            this.incomeTypeValue = incomeType.getIncome_type_value();
        }
    }

    public Income(BigDecimal incomeAmount, int incomeTypeValue, String incomeDescription) {

        this.incomeAmount = incomeAmount;
        this.incomeTypeValue = incomeTypeValue;
        this.incomeReference = incomeDescription;
    }
}
