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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long customerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updateDate")
    private Date updateDate = new Date();

    @NotNull(message = "Customer full name cannot be null")
    @Size(min = 3, message = "Customer name must have at least three characters")
    @Column(name = "customerFullName", nullable = false)
    private String customerFullName;

    @NotNull(message = "Customer phone number cannot be null")
    @Pattern(regexp = "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}", message = "Invalid Phone Number Entered")
    @Column(name = "customerPhoneNumber", nullable = false, unique = true)
    private String customerPhoneNumber;

    @Email(message = "Invalid Email Address Entered", regexp = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")
    @Column(name = "customerEmail", unique = true)
    private String customerEmail;

    @Column(name = "createdBy")
    private String createdBy;
}
