package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.ENUM.EXPENSE_TYPE;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @NotNull(message = "Expense type cannot be null")
    @Column(name = "expenseType", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EXPENSE_TYPE expenseType;

    @NotNull(message = "Expense amount cannot be null")
    @Column(name = "expenseAmount", nullable = false)
    private BigDecimal expenseAmount;

    @NotNull(message = "Expense description cannot be null")
    @Column(name = "expenseDescription", nullable = false)
    private String expenseDescription;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approvedBy")
    private String approvedBy;
}
