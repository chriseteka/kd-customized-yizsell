package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warehouseId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Warehouse address cannot be null")
    @Size(min = 3, message = "Address must contain at least two characters")
    @Column(name = "warehouse-address", nullable = false)
    private String warehouseAddress;

    @ManyToMany
    @JoinTable(name = "shops-in-warehouse", joinColumns = @JoinColumn(name = "warehouseId"), inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Set<Shop> shops = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "stocks-in-warehouse", joinColumns = @JoinColumn(name = "warehouseId"), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Set<Stock> stocks = new HashSet<>();
}
