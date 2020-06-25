package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/25/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Income implements Serializable {

    private Long incomeId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private BigDecimal incomeAmount;
    private String incomeReference;
    private String createdBy;
    private Boolean approved;
    private Date approvedDate;
    private String approvedBy;
    private int incomeTypeValue;
    private String incomeTypeVal;
    private INCOME_TYPE incomeType;

    public Income(Long incomeId, Date createdDate, Date createdTime, Date updateDate, BigDecimal incomeAmount,
              String incomeReference, String createdBy, Boolean approved, Date approvedDate, String approvedBy,
              int incomeTypeValue, String incomeTypeVal, INCOME_TYPE incomeType) {
        this.incomeId = incomeId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.incomeAmount = incomeAmount;
        this.incomeReference = incomeReference;
        this.createdBy = createdBy;
        this.approved = approved;
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.incomeTypeValue = incomeTypeValue;
        this.incomeTypeVal = incomeTypeVal;
        this.incomeType = incomeType;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income fromDTO(){
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income incomeFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income();
        incomeFromDTO.setIncomeId(this.getIncomeId());
        incomeFromDTO.setCreatedDate(this.getCreatedDate());
        incomeFromDTO.setCreatedTime(this.getCreatedTime());
        incomeFromDTO.setUpdateDate(this.getUpdateDate());
        incomeFromDTO.setIncomeAmount(this.getIncomeAmount());
        incomeFromDTO.setIncomeReference(this.getIncomeReference());
        incomeFromDTO.setCreatedBy(this.getCreatedBy());
        incomeFromDTO.setApproved(this.approved);
        incomeFromDTO.setApprovedDate(this.getApprovedDate());
        incomeFromDTO.setApprovedBy(this.getApprovedBy());
        incomeFromDTO.setIncomeTypeValue(this.incomeTypeValue);
        incomeFromDTO.setIncomeTypeVal(this.incomeTypeVal);
        incomeFromDTO.setIncomeType(this.getIncomeType());

        return incomeFromDTO;
    }
}
