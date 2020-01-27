package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 1/19/2020
 * @email chriseteka@gmail.com
 */
//Models created by business owners only for the different discount types they want
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DiscountModels")
public class DiscountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DiscountModelId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    @NotEmpty(message = "Discount name cannot be empty")
    private String discountName;

    //Optional (default is 0.0)
    private double discountPercentage = 0.0;

    private String createdBy;
}
