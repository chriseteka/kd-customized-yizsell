package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long warehouseId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @NotNull(message = "Warehouse name cannot be null")
    @Size(min = 3, message = "Warehouse name must contain at least two characters")
    @Column(name = "warehouseName", nullable = false, unique = true)
    private String warehouseName;

    @NotNull(message = "Warehouse address cannot be null")
    @Size(min = 3, message = "Address must contain at least two characters")
    @Column(name = "warehouseAddress", nullable = false)
    private String warehouseAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "businessOwnerWarehouses", joinColumns = @JoinColumn(name = "businessOwnerId"), inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    @JsonIgnoreProperties("warehouses")
    private BusinessOwner businessOwner;

//    @JsonIgnore
//    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "warehouses")
//    private Set<Shop> shops = new HashSet<>();

//    @JsonIgnore
//    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "warehouses")
//    private Set<Stock> stocks = new HashSet<>();
}
