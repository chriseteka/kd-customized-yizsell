package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 5/14/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Promos")
public class Promo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promoId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    private String createdBy;

    @Size(min = 3, message = "Promo name must be three characters or above")
    private String promoName;

    @Min(value = 1, message = "Minimum purchases must be set to one or over")
    private int minimumPurchase;

    @Min(value = 1, message = "Reward per Minimum purchases must be set to one or over")
    private int rewardPerMinimum;

    private boolean isActive = true;

    @OneToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "promoStock", joinColumns = @JoinColumn(name = "promoId",
            nullable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Set<ShopStocks> promoOnStock = new HashSet<>();

    @PrePersist
    private void setTransients(){
        this.createdBy = AuthenticatedUserDetails.getUserFullName();
    }
}
