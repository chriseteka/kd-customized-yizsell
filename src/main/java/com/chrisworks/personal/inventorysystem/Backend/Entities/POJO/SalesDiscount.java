package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Constants.DEFAULT_DISCOUNTS;

//Every sales can have a set of discounts, this object can also be returned to business owners as well
//It will be used during end of the day computation.
/**
 * @author Chris_Eteka
 * @since 1/19/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SalesDiscounts")
public class SalesDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SalesDiscountId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    private String createdBy;

    private String invoiceNumber;

    //Optional field, can be empty.
    private String discountIssuedTo;

    private BigDecimal discountAmount;

    private Double discountPercentage;

    private String reasonForDiscount;

    private String discountType;

    @PrePersist
    void validateDiscountType(){

        if (!DEFAULT_DISCOUNTS.contains(discountType) && (reasonForDiscount == null
                || StringUtils.isEmpty(reasonForDiscount)))
            throw new InventoryAPIOperationException("Reason for discount error",
                    "Please pass in a valid reason for this discount before you can proceed", null);
    }
}
