package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Sellers")
public class Seller implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long sellerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @Size(min = 3, message = "Name must contain at least three characters")
    @Column(name = "sellerFullName", nullable = false)
    private String sellerFullName;

    @Email(message = "Invalid Email Address Entered")
    @Column(name = "sellerEmail", unique = true, nullable = false)
    private String sellerEmail;

    @Size(min = 5, max = 15, message = "Invalid Phone Number Entered")
    @Column(name = "sellerPhoneNumber", nullable = false)
    private String sellerPhoneNumber;

    @Size(min = 4, message = "Password must contain at least four characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "sellerPassword", nullable = false)
    private String sellerPassword;

    @Size(min = 3, message = "Seller address must contain at least three characters")
    @Column(name = "sellerAddress", nullable = false)
    private String sellerAddress;

    @Column(name = "lastLoginDate")
    @Temporal(TemporalType.DATE)
    private Date lastLoginDate;

    @Column(name = "lastLoginTime")
    @Temporal(TemporalType.TIME)
    private Date lastLoginTime;

    @Column(name = "lastLogoutTime")
    @Temporal(TemporalType.TIME)
    private Date lastLogoutTime;

    //Seller can be assigned to a shop
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "sellersInShop", joinColumns = @JoinColumn(name = "sellerId"),
            inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Shop shop;

    //Seller can be assigned to a warehouse but not both
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "sellersInWarehouse", joinColumns = @JoinColumn(name = "sellerId"),
            inverseJoinColumns = @JoinColumn(name = "warehouseId"))
    private Warehouse warehouse;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @Column(name = "isChatRegistered")
    private Boolean isRegisteredForChat = false;

    @Column(name = "createdBy", nullable = false)
    private String createdBy;

    @Basic
    @JsonIgnore
    @Column(name = "accountType")
    private int accountTypeValue;

    //Seller can be a WAREHOUSE_ATTENDANT or a SHOP_SELLER
    @Transient
    private ACCOUNT_TYPE accountType;

    @PostLoad
    void fillTransient() {
        if (accountTypeValue > 0) {
            this.accountType = ACCOUNT_TYPE.of(accountTypeValue);
        }
    }

    @PrePersist
    void fillPersistent() {
        if (accountType != null) {
            this.accountTypeValue = accountType.getAccount_type_value();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(ACCOUNT_TYPE.STAFF::toString);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return getSellerPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return getSellerEmail();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
