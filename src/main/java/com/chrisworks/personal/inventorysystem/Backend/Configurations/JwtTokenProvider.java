package com.chrisworks.personal.inventorysystem.Backend.Configurations;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.SECRET;
import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.TOKEN_EXPIRATION_TIME;

@Component
public class JwtTokenProvider {

    private Date now = new Date(System.currentTimeMillis());

    private Date expiryDate = new Date(now.getTime() + TOKEN_EXPIRATION_TIME);

    //Generate token for business owner login
    public String generateBusinessOwnerToken(BusinessOwner userDetails){

        String userId = Long.toString(userDetails.getBusinessOwnerId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(userDetails.getBusinessOwnerId())));
        claims.put("username", userDetails.getUsername());
        claims.put("accountType", userDetails.getAccountType().toString());

        return jwtToken(userId, claims);
    }

    private String jwtToken(String userId, Map<String, Object> claims) {
        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public String generateSellerToken(Seller userDetails) {

        String userId = Long.toString(userDetails.getSellerId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", (Long.toString(userDetails.getSellerId())));
        claims.put("username", userDetails.getUsername());
        claims.put("accountType", userDetails.getAccountType().toString());

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
}
