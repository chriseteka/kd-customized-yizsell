package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

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
@Table(name = "Shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long shopId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @NotNull(message = "shop name cannot be null")
    @Size(min = 3, message = "Shop name must contain at least three characters")
    @Column(name = "shopName", nullable = false, unique = true)
    private String shopName;

    @NotNull(message = "shop address cannot be null")
    @Size(min = 3, message = "Address must contain at least three characters")
    @Column(name = "shopAddress", nullable = false)
    private String shopAddress;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "shopsInWarehouse", joinColumns = @JoinColumn(name = "warehouseId"), inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Set<Warehouse> warehouses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sellersInShop", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "sellerId"))
    private Set<Seller> sellers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "returnedSales", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "returnedStockId"))
    private Set<ReturnedStock> returnedSales = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "shopExpenses", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "expenseId"))
    private Set<Expense> expenses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "shopIncome", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "incomeId"))
    private Set<Income> income = new HashSet<>();
}
