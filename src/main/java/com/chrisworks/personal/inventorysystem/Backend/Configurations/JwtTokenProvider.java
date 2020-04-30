package com.chrisworks.personal.inventorysystem.Backend.Configurations;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.RefreshTokenService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.SECRET;
import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.TOKEN_EXPIRATION_TIME;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RefreshTokenService refreshTokenService;

    //Generate token for business owner login
    public String generateBusinessOwnerToken(BusinessOwner userDetails){

        String userId = Long.toString(userDetails.getBusinessOwnerId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(userDetails.getBusinessOwnerId())));
        claims.put("businessName", userDetails.getBusinessName());
        claims.put("fullName", userDetails.getBusinessOwnerFullName());
        claims.put("username", userDetails.getUsername());
        claims.put("phoneNumber", userDetails.getBusinessOwnerPhoneNumber());
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
        if (userDetails.getShop() != null)
            claims.put("businessName", userDetails.getShop().getBusinessOwner().getBusinessName());
        if (userDetails.getWarehouse() != null)
            claims.put("businessName", userDetails.getWarehouse().getBusinessOwner().getBusinessName());
        claims.put("fullName", userDetails.getSellerFullName());
        claims.put("username", userDetails.getUsername());
        claims.put("isActive", userDetails.getIsActive());
        claims.put("accountType", userDetails.getAccountType().toString());
        claims.put("refreshToken", refreshTokenService.generateRefreshToken(userDetails.getSellerEmail()));

        return jwtToken(userId, claims);
    }

    //Validate the token
    public boolean validateToken(String token){

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
    public Long userIdFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        String id = (String) claims.get("id");

        return Long.parseLong(id);
    }

    //Get user Id from the token
    public String userEmailFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

       return  (String) claims.get("username");
    }

    //Get user Id from the token
    public ACCOUNT_TYPE userAccountTypeJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        String accountType = (String) claims.get("accountType");

        return ACCOUNT_TYPE.valueOf(accountType);
    }

    public Boolean hasWarehouseFromJwt(String token){

        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

        if (claims.get("hasWarehouse") == null) return false;

        return  (Boolean) claims.get("hasWarehouse");
    }
}
