package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 8/1/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Column(name = "planName")
    private String planName;

    @Column(name = "numberOfStaff")
    private int numberOfStaff;

    @Column(name = "numberOfWarehouses")
    private int numberOfWarehouses;

    @Column(name = "numberOfShops")
    private int numberOfShops;

    @JsonManagedReference
    @OneToMany(mappedBy = "plan", targetEntity = BusinessOwner.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BusinessOwner> businesses = new HashSet<>();

}
