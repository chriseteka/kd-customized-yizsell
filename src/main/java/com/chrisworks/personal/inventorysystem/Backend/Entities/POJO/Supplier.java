package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
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
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long SupplierId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Size(min = 3, message = "Name must contain at least three characters")
    @Column(name = "supplierFullName", nullable = false)
    private String supplierFullName;

    @Size(min = 5, max = 15, message = "Invalid Phone Number Entered")
    @Column(name = "supplierPhoneNumber", nullable = false)
    private String supplierPhoneNumber;

    @Email(message = "Invalid Email Address Entered")
    @Column(name = "supplierEmail")
    private String supplierEmail;

    @Column(name = "createdBy")
    private String createdBy;

}
