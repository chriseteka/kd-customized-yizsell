package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
@Table(name = "Sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sellerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Seller full name cannot be null")
    @Size(min = 3, message = "Name must contain at least three characters")
    @Column(name = "seller-full-name", nullable = false)
    private String sellerFullName;

    @Email(message = "Invalid Email Address Entered", regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")
    @Column(name = "seller-email", unique = true)
    private String sellerEmail;

    @NotNull(message = "seller phone number cannot be null")
    @Pattern(regexp = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}", message = "Invalid Phone Number Entered")
    @Column(name = "seller-phone-number", nullable = false)
    private String sellerPhoneNumber;

    @NotNull(message = "Seller password cannot be null")
    @Size(min = 4, message = "Password must contain at least four characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "seller-password", nullable = false)
    private String sellerPassword;

    @NotNull(message = "Seller address cannot be null")
    @Size(min = 3, message = "Seller address must contain at least three characters")
    @Column(name = "seller-address", nullable = false)
    private String sellerAddress;

    @ManyToMany
    @JoinTable(name = "seller-invoices", joinColumns = @JoinColumn(name = "sellerId"), inverseJoinColumns = @JoinColumn(name = "invoiceId"))
    private Set<Invoice> invoices = new HashSet<>();

}
