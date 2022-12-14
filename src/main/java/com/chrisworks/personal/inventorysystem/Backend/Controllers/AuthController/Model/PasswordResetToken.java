package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 12/16/2019
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PasswordResetTokens")
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = BusinessOwner.class, fetch = FetchType.EAGER)
    @JoinTable(name = "businessOwnerResetToken", joinColumns = @JoinColumn(name = "tokenId"), inverseJoinColumns = @JoinColumn(name = "businessOwnerId"))
    private BusinessOwner businessOwner;

    private Date expiryDate = calculateExpiryDate();

    public PasswordResetToken(BusinessOwner user, String token) {
        this.businessOwner = user;
        this.token = token;
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, PasswordResetToken.EXPIRATION);
        return new Date(cal.getTime().getTime());
    }
}
