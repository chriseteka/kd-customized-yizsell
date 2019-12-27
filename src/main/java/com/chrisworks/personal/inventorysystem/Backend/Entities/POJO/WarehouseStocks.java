package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
//This class holds stocks for business owners that has warehouse, stocks are added first into their warehouse
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "WarehouseStocks")
public class WarehouseStocks extends Stock {

    @Column(name = "possibleQuantityRemaining")
    private int possibleQuantityRemaining;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "stocksInWarehouse", joinColumns = @JoinColumn(name = "warehouseId"), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Warehouse warehouse;
}
