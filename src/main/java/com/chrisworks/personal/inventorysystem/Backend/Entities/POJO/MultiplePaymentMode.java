package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 2/10/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MultiplePaymentModes")
public class MultiplePaymentMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MultiplePaymentModeId;

    @Temporal(TemporalType.DATE)
    @Column(name = "createdDate")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    @Column(name = "createdTime")
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "updatedDate")
    private Date updateDate = new Date();

    @Column(name = "createdBy")
    private String createdBy;

    @Basic
    @JsonIgnore
    @Column(name = "paymentMode", nullable = false)
    private int paymentModeValue;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty(message = "Payment type cannot be null")
    private String paymentModeVal;

    @Transient
    private PAYMENT_MODE paymentMode;

    @DecimalMin(value = "0")
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @PostLoad
    void fillTransient() {
        if (paymentModeValue > 0) {
            this.paymentMode = PAYMENT_MODE.of(paymentModeValue);
        }
    }

    @PrePersist
    void fillPersistent() {

        this.createdBy = AuthenticatedUserDetails.getUserFullName();
        this.paymentModeValue = Integer.parseInt(this.paymentModeVal);

        if (paymentModeValue > 0) {
            this.paymentMode = PAYMENT_MODE.of(paymentModeValue);
        }
    }

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }
}
