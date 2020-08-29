package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.REFRESH_TOKEN_EXPIRATION_DAYS;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.futureDate;

/**
 * @author Chris_Eteka
 * @since 4/30/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "RefreshTokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long refreshTokenId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIME)
    private Date createdTime = new Date();

    @Temporal(TemporalType.DATE)
    private Date updateDate = new Date();

    @Column(name = "refreshToken")
    private String token;

    @Column(name = "expirationDate")
    private Date expirationDate;

    @Column(name = "userEmail")
    private String userEmail;

    public RefreshToken(String email) {
        this.userEmail = email;
    }

    @PrePersist
    void fillTransients(){
        this.token = generateUUID();
        this.expirationDate = futureDate(REFRESH_TOKEN_EXPIRATION_DAYS);
    }

    @PreUpdate
    void changeUpdatedDate(){
        this.setUpdateDate(new Date());
    }

    private String generateUUID(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[128];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
