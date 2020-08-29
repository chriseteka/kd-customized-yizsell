package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Setter
@Getter
@ToString
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

    private String waybillId = "";

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "stocksInProcurement", joinColumns = @JoinColumn(name = "procurementId",
            nullable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Set<ProcuredStock> stocks;

    private BigDecimal amount = BigDecimal.ZERO;

    private boolean recordAsExpense = false;

    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "procurementSuppliers", joinColumns = @JoinColumn(name = "procurementId"),
            inverseJoinColumns = @JoinColumn(name = "supplierId"))
    private Supplier supplier;

    private boolean movedToWarehouse = false;

    private Long movedToWarehouseId;

    public boolean equals(Procurement p){

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

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }

    public boolean procurementAmountInAccurate() {
        return is(this.stocks.stream().map(ProcuredStock::getStockTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)).notEq(this.amount);
    }

    public void verifyNotMovedYet() {
        if (this.isMovedToWarehouse()) throw new InventoryAPIOperationException("Procurement already moved",
                "Procurement with id: " + this.getProcurementId() + " has already been moved to warehouse: "
                        + this.getMovedToWarehouseId(), null);
    }
}
