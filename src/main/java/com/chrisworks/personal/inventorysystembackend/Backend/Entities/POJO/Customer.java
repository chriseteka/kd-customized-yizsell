package com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "created-date")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "created-time")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updated-date")
    private Date updateDate = new Date();

    @NotNull(message = "Customer full name cannot be null")
    @Size(min = 3, message = "Customer name must have at least three characters")
    @Column(name = "customer-full-name", nullable = false)
    private String customerFullName;

    @NotNull(message = "Customer phone number cannot be null")
    @Pattern(regexp = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}", message = "Invalid Phone Number Entered")
    @Column(name = "customer-phone-number", nullable = false, unique = true)
    private String customerPhoneNumber;

    @Email(message = "Invalid Email Address Entered", regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")
    @Column(name = "customer-email", unique = true)
    private String customerEmail;

    @NotNull(message = "Created by field cannot be null")
    @Column(name = "created-by", nullable = false)
    private String createdBy;
}
