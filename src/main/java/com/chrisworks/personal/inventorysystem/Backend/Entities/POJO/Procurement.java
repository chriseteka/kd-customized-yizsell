package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "procurements")
public class Procurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long procurementId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    @NotEmpty(message = "Please provide the waybill id used to make this procurement")
    private String waybillId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "stocksInProcurement", joinColumns = @JoinColumn(name = "procurementId",
            nullable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Set<ProcuredStock> stocks;

    @DecimalMin(value = "0.0", inclusive = false, message = "Procurement amount must be greater than zero")
    private BigDecimal amount;

    private boolean recordAsExpense = false;

    private String createdBy;

    public boolean equals(Procurement p){
        ArrayList<ProcuredStock> procuredStocks = new ArrayList<>(this.stocks);
        ArrayList<ProcuredStock> incomingStock = new ArrayList<>(p.getStocks());
        procuredStocks.sort(Comparator.comparing(ProcuredStock::getStockName));
        incomingStock.sort(Comparator.comparing(ProcuredStock::getStockName));

        return this.waybillId.equalsIgnoreCase(p.getWaybillId())
                && is(this.amount).eq(p.getAmount())
                && this.recordAsExpense == p.isRecordAsExpense()
                && this.stocks.stream().allMatch(s -> p.getStocks().stream().anyMatch(ps -> ps.equals(s)));
    }

    @PrePersist
    private void executeBeforeSave(){

        if (procurementAmountInAccurate()){
            throw new InventoryAPIOperationException("Total Procurement Amount Mismatch",
                    "Total procurement amount does not tally with the sum of the individual stock total amount", null);
        }
        this.createdBy = AuthenticatedUserDetails.getUserFullName();
    }

    public boolean procurementAmountInAccurate() {
        return is(this.stocks.stream().map(ProcuredStock::getStockTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)).notEq(this.amount);
    }
}
