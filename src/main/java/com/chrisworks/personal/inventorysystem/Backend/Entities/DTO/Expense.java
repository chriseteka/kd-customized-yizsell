package com.chrisworks.personal.inventorysystem.Backend.Entities.DTO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 6/26/2020
 * @email chriseteka@gmail.com
 */
@Data
public class Expense {

    private Long expenseId;
    private Date createdDate;
    private Date createdTime;
    private Date updateDate;
    private BigDecimal expenseAmount;
    private String expenseDescription;
    private String createdBy;
    private Boolean approved;
    private Date approvedDate;
    private String approvedBy;
    private int expenseTypeValue;
    private String expenseTypeVal;
    private EXPENSE_TYPE expenseType;

    public Expense(Long expenseId, Date createdDate, Date createdTime, Date updateDate, BigDecimal expenseAmount,
               String expenseDescription, String createdBy, Boolean approved, Date approvedDate, String approvedBy,
               int expenseTypeValue, String expenseTypeVal, EXPENSE_TYPE expenseType) {
        this.expenseId = expenseId;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.updateDate = updateDate;
        this.expenseAmount = expenseAmount;
        this.expenseDescription = expenseDescription;
        this.createdBy = createdBy;
        this.approved = approved;
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.expenseTypeValue = expenseTypeValue;
        this.expenseTypeVal = expenseTypeVal;
        this.expenseType = expenseType;
    }

    public com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense fromDTO(){
        com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense expenseFromDTO =
            new com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense();
        expenseFromDTO.setExpenseId(this.getExpenseId());
        expenseFromDTO.setCreatedDate(this.getCreatedDate());
        expenseFromDTO.setCreatedTime(this.getCreatedTime());
        expenseFromDTO.setUpdateDate(this.getUpdateDate());
        expenseFromDTO.setExpenseAmount(this.getExpenseAmount());
        expenseFromDTO.setExpenseDescription(this.getExpenseDescription());
        expenseFromDTO.setCreatedBy(this.getCreatedBy());
        expenseFromDTO.setApproved(this.getApproved());
        expenseFromDTO.setApprovedDate(this.getApprovedDate());
        expenseFromDTO.setApprovedBy(this.getApprovedBy());
        expenseFromDTO.setExpenseTypeValue(this.getExpenseTypeValue());
        expenseFromDTO.setExpenseTypeVal(this.getExpenseTypeVal());
        expenseFromDTO.setExpenseType(this.getExpenseType());

        return expenseFromDTO;
    }
}
