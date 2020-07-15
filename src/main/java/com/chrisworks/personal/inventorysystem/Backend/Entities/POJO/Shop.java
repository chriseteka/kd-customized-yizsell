package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @Size(min = 3, message = "Shop name must contain at least three characters")
    @Column(name = "shopName", nullable = false)
    private String shopName;

    @Size(min = 3, message = "Address must contain at least three characters")
    @Column(name = "shopAddress", nullable = false)
    private String shopAddress;

    @Column(name = "createdBy", nullable = false)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "businessOwnerShops", joinColumns = @JoinColumn(name = "shopId"),
            inverseJoinColumns = @JoinColumn(name = "businessOwnerId"))
    @JsonIgnore
    private BusinessOwner businessOwner;
}
