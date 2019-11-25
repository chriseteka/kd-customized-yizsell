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
@Table(name = "Shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "shop address cannot be null")
    @Size(min = 3, message = "Address must contain at least three characters")
    @Column(name = "shop-address", nullable = false)
    private String shopAddress;

    @ManyToMany
    @JoinTable(name = "sellers-in-shop", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "sellerId"))
    private Set<Seller> sellers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "returned-sales", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "returnedStockId"))
    private Set<ReturnedStock> returnedSales = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "shop-expenses", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "expenseId"))
    private Set<Expense> expenses = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "shop-income", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "incomeId"))
    private Set<Income> income = new HashSet<>();
}
