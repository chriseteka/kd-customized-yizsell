package com.chrisworks.personal.inventorysystem.Backend.Configurations;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.RefreshTokenService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${JWT_SECRET}") private String SECRET;
    @Value("${JWT_EXPIRATION_TIME}") private Long TOKEN_EXPIRATION_TIME; //3days

    private final RefreshTokenService refreshTokenService;

    //Generate token for business owner login
    public String generateBusinessOwnerToken(BusinessOwner userDetails){

        String userId = Long.toString(userDetails.getBusinessOwnerId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(userDetails.getBusinessOwnerId())));
        claims.put("businessName", userDetails.getBusinessName());
        claims.put("businessEmail", userDetails.getBusinessOwnerEmail());
        claims.put("fullName", userDetails.getBusinessOwnerFullName());
        claims.put("username", userDetails.getUsername());
        claims.put("phoneNumber", businessPhones(userDetails));
        claims.put("isTrialAccount", userDetails.getIsTrialAccount());
        claims.put("expirationDate", userDetails.getExpirationDate());
        claims.put("isVerified", userDetails.getVerified());
        claims.put("isActive", userDetails.getIsActive());
        claims.put("hasWarehouse", userDetails.getHasWarehouse());
        claims.put("accountType", userDetails.getAccountType().toString());
        claims.put("refreshToken", refreshTokenService.generateRefreshToken(userDetails.getBusinessOwnerEmail()));

        return jwtToken(userId, claims);
    }

    private String jwtToken(String userId, Map<String, Object> claims) {

        Date now = new Date(System.currentTimeMillis());

        Date expiryDate = new Date(now.getTime() + TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public String generateSellerToken(Seller userDetails) {

        if (userDetails.getAccountType() == null) throw new InventoryAPIOperationException("Seller cannot login",
                "Seller may have not been assigned a warehouse or shop, hence cannot login", null);

        String userId = Long.toString(userDetails.getSellerId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(userDetails.getSellerId())));
        BusinessOwner businessOwner = null;
        if (userDetails.getShop() != null) businessOwner = userDetails.getShop().getBusinessOwner();
        if (userDetails.getWarehouse() != null) businessOwner = userDetails.getWarehouse().getBusinessOwner();
        if (businessOwner != null) {
            claims.put("businessName", businessOwner.getBusinessName());
            claims.put("businessPhone", businessPhones(businessOwner));
            claims.put("businessEmail", businessOwner.getBusinessOwnerEmail());
        }
        claims.put("fullName", userDetails.getSellerFullName());
        claims.put("username", userDetails.getUsername());
        claims.put("isActive", userDetails.getIsActive());
        claims.put("accountType", userDetails.getAccountType().toString());
        claims.put("refreshToken", refreshTokenService.generateRefreshToken(userDetails.getSellerEmail()));

        return jwtToken(userId, claims);
    }

    //Validate the token
    boolean validateToken(String token){

        try {

            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        }catch (SignatureException se){
            System.out.println("Jwt signature error");
        }catch (MalformedJwtException mje){
            System.out.println("Malformed jwt");
        }catch (ExpiredJwtException eje){
            System.out.println("Expired jwt");
        }catch (UnsupportedJwtException uje){
            System.out.println("unsupported jwt");
        }catch (IllegalArgumentException iae){
            System.out.println("Illegal args");
        }
        return false;
    }

    //Get user Id from the token
    Long userIdFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        String id = (String) claims.get("id");

        return Long.parseLong(id);
    }

    //Get user Email from the token
    String userEmailFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

       return  (String) claims.get("username");
    }

    //Get user Account type from the token
    ACCOUNT_TYPE userAccountTypeJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        String accountType = (String) claims.get("accountType");

        return ACCOUNT_TYPE.valueOf(accountType);
    }

    Boolean hasWarehouseFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        if (claims.get("hasWarehouse") == null) return false;

        return  (Boolean) claims.get("hasWarehouse");
    }

    String businessIdFromJwt(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        return (String) claims.get("businessEmail");
    }

    private String businessPhones(BusinessOwner businessOwner){

        String businessNumber = businessOwner.getBusinessOwnerPhoneNumber();
        String otherNumbers = businessOwner.getOtherNumbers();
        if (StringUtils.hasLength(otherNumbers))
            return String.join(",", businessNumber, otherNumbers);
        else return businessNumber;
    }
}
