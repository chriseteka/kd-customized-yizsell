package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.ENUM.INCOME_TYPE;
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

    @NotNull(message = "income amount cannot be null")
    @Column(name = "incomeAmount", nullable = false)
    private BigDecimal incomeAmount;

    @Column(name = "incomeReference")
    private String incomeReference = "Nil";

    @NotNull(message = "Income type cannot be null")
    @Column(name = "incomeType", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private INCOME_TYPE incomeType;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "approvedBy")
    private String approvedBy;

}
