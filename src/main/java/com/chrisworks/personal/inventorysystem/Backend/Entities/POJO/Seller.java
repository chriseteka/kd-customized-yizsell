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

    @NotNull(message = "Seller full name cannot be null")
    @Size(min = 3, message = "Name must contain at least three characters")
    @Column(name = "sellerFullName", nullable = false)
    private String sellerFullName;

    @NotNull(message = "Seller's email cannot be null")
    @Email(message = "Invalid Email Address Entered")
    @Column(name = "sellerEmail", unique = true, nullable = false)
    private String sellerEmail;

    @NotNull(message = "seller phone number cannot be null")
    @Pattern(regexp = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}", message = "Invalid Phone Number Entered")
    @Column(name = "sellerPhoneNumber", nullable = false)
    private String sellerPhoneNumber;

    @NotNull(message = "Seller password cannot be null")
    @Size(min = 4, message = "Password must contain at least four characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "sellerPassword", nullable = false)
    private String sellerPassword;

    @NotNull(message = "Seller address cannot be null")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "sellersInShop", joinColumns = @JoinColumn(name = "sellerId"), inverseJoinColumns = @JoinColumn(name = "shopId"))
    private Shop shop;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sellerInvoices", joinColumns = @JoinColumn(name = "sellerId"), inverseJoinColumns = @JoinColumn(name = "invoiceId"))
    private Set<Invoice> invoices = new HashSet<>();

    @Basic
    @JsonIgnore
    @Column(name = "accountType", updatable = false)
    private int accountTypeValue;

    @Transient
    private ACCOUNT_TYPE accountType = ACCOUNT_TYPE.SELLER;

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
        return Collections.singleton(ACCOUNT_TYPE.SELLER::toString);
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
