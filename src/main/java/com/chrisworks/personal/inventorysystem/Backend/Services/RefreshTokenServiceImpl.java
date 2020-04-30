package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.RefreshToken;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.RefreshTokenRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getDateDifferenceInDays;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final SellerRepository sellerRepository;

    @Override
    public String generateRefreshToken(String email) {

        RefreshToken refreshToken = refreshTokenRepository.findDistinctByUserEmail(email);

        //If token exists and token has not expired
        if (refreshToken != null)
            if (getDateDifferenceInDays(new Date(), refreshToken.getExpirationDate()) >= 4)
                return refreshToken.getToken();
            else refreshTokenRepository.delete(refreshToken);

        return refreshTokenRepository.save(new RefreshToken(email)).getToken();
    }

    @Override
    public Object refreshLoggedInUserToken(String refreshToken, ACCOUNT_TYPE accountType) {

        RefreshToken token = fetchRefreshToken(refreshToken, accountType);
        if (token == null)
            throw new InventoryAPIOperationException("Token cannot be refreshed",
                    "Could not refresh user jwt token as user have no refresh token", null);

        if (getDateDifferenceInDays(new Date(), token.getExpirationDate()) <= 0)
            throw new InventoryAPIOperationException("Refresh Token Expired",
                    "Refresh token expired, please log out and log in again", null);

        String userEmail = token.getUserEmail();

        if (accountType.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return businessOwnerRepository.findDistinctByBusinessOwnerEmail(userEmail);
        else if (accountType.equals(ACCOUNT_TYPE.SHOP_SELLER) || accountType.equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT))
            return sellerRepository.findDistinctBySellerEmail(userEmail);

        return null;
    }

    private RefreshToken fetchRefreshToken(String token, ACCOUNT_TYPE accountType){

        if (token.isEmpty() || accountType == null)
            throw new InventoryAPIOperationException("Auth User not found",
                    "This operation cannot proceed as required data was not passed to the function", null);

        return refreshTokenRepository.findDistinctByToken(token);
    }
}
