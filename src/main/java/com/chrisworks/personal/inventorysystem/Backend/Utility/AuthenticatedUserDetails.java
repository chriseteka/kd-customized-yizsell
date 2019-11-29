package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
//This object is created on successful authentication, and they form the requirements(claims) for jwt
@AllArgsConstructor
//@NoArgsConstructor
public class AuthenticatedUserDetails {

    private static String userId;

    private static String userFullName;

    private static ACCOUNT_TYPE account_type;

    public static String getUserFullName() {
        return userFullName;
    }

    public static String getUserId() {
        return userId;
    }

    public static ACCOUNT_TYPE getAccount_type(){ return account_type; }
}
