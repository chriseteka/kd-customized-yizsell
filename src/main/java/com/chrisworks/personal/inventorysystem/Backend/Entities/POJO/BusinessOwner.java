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
import java.math.BigDecimal;
import java.util.*;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.futureDate;
import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIME;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "businessOwners")
public class BusinessOwner implements UserDetails {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long businessOwnerId;

    @Temporal(DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @Size(min = 3, message = "Business Name must be at least three characters")
    @Column(name = "businessName", nullable = false)
    private String businessName;

    @Size(min = 3, message = "Business Owner Full Name must be at least three characters")
    @Column(name = "businessOwnerFullName", nullable = false)
    private String businessOwnerFullName;

    @Email(message = "Invalid Email Address Entered")
    @Column(name = "businessOwnerEmail", unique = true, nullable = false)
    private String businessOwnerEmail;

    @Size(min = 5, max = 15, message = "Invalid Phone Number Entered")
    @Column(name = "businessOwnerPhoneNumber", nullable = false)
    private String businessOwnerPhoneNumber;

    @Size(min = 4, message = "Password must contain at least four characters")
    @Column(name = "businessOwnerPassword", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String businessOwnerPassword;

    @Column(name = "businessTotalIncome", precision = 2)
    private BigDecimal businessTotalIncome = BigDecimal.ZERO;

    @Column(name = "businessTotalExpenses", precision = 2)
    private BigDecimal businessTotalExpenses = BigDecimal.ZERO;

    @Column(name = "businessTotalProfit", precision = 2)
    private BigDecimal businessTotalProfit = BigDecimal.ZERO;

    //Hash this before storage to the database
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Temporal(DATE)
    @Column(name = "expirationDate")
    private Date expirationDate = futureDate(30);

    @Column(name = "isActive")
    private Boolean isActive = false;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "hasWarehouse")
    private Boolean hasWarehouse = false;

    @Basic
    @JsonIgnore
    @Column(name = "accountType", updatable = false)
    private int accountTypeValue;

    @Transient
    private ACCOUNT_TYPE accountType = ACCOUNT_TYPE.BUSINESS_OWNER;

    @Column(name = "isTrialAccount")
    private Boolean isTrialAccount = true;

    @Column(name = "isChatRegistered")
    private Boolean isRegisteredForChat = false;

    @Column(name = "otherNumbers")
    private String otherNumbers;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name="planId")
    private Plan plan;

    @Transient
    private String subscription;

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
        if(!isTrialAccount){
            if (subscription.equalsIgnoreCase("THREE")) {
                this.expirationDate = futureDate(95);
                this.isTrialAccount = false;
            }
            if (subscription.equalsIgnoreCase("SIX")){
                this.expirationDate = futureDate(185);
                this.isTrialAccount = false;
            }
            if (subscription.equalsIgnoreCase("TWELVE")){
                this.expirationDate = futureDate(366);
                this.isTrialAccount = false;
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(ACCOUNT_TYPE.BUSINESS_OWNER::toString);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return getBusinessOwnerPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return getBusinessOwnerEmail();
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

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }
}
