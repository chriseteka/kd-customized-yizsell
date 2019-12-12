package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
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
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long expenseId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @DecimalMin(value = "0.0", inclusive = false, message = "Expense must be greater than zero")
    @Column(name = "expenseAmount", nullable = false)
    private BigDecimal expenseAmount;

    @Size(min = 3, message = "Expense description must be greater than three characters")
    @Column(name = "expenseDescription", nullable = false)
    private String expenseDescription;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "approvedDate")
    private Date approvedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "shopExpenses", joinColumns = @JoinColumn(name = "expenseId"), inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Shop shop;

    @Basic
    @JsonIgnore
    @Column(name = "expenseType", nullable = false)
    private int expenseTypeValue;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty(message = "Expense type cannot be null")
    private String expenseTypeVal;

    @Transient
    private EXPENSE_TYPE expenseType;

    @PostLoad
    void fillTransient() {
        if (expenseTypeValue > 0) {
            this.expenseType = EXPENSE_TYPE.of(expenseTypeValue);
        }
    }

    @PrePersist
    void fillPersistent() {
        if (expenseType != null) {
            this.expenseTypeValue = expenseType.getExpense_type_value();
        }
        if (expenseTypeValue > 0) {
            this.expenseType = EXPENSE_TYPE.of(expenseTypeValue);
        }
    }

    public Expense(int expenseTypeValue, BigDecimal expenseAmount, String expenseDescription) {

        this.expenseTypeVal = String.valueOf(expenseTypeValue);
        this.expenseTypeValue = expenseTypeValue;
        this.expenseAmount = expenseAmount;
        this.expenseDescription = expenseDescription;
    }
}
