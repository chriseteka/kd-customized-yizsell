package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;

/**
 * @author Chris_Eteka
 * @since 4/30/2020
 * @email chriseteka@gmail.com
 */
public interface RefreshTokenService {

    String generateRefreshToken(String email);

    Object refreshLoggedInUserToken(String refreshToken, ACCOUNT_TYPE accountType);
}
