package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @Size(min = 3, message = "Warehouse name must contain at least two characters")
    @Column(name = "warehouseName", nullable = false, unique = true)
    private String warehouseName;

    @Size(min = 3, message = "Address must contain at least two characters")
    @Column(name = "warehouseAddress", nullable = false)
    private String warehouseAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "businessOwnerWarehouses", joinColumns = @JoinColumn(name = "businessOwnerId"), inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    @JsonIgnore
    private BusinessOwner businessOwner;
}
