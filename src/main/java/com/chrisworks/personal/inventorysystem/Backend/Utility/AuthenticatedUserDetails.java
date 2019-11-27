package com.chrisworks.personal.inventorysystem.Backend.Utility;

import lombok.NoArgsConstructor;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@NoArgsConstructor
public class AuthenticatedUserDetails {

    private static String userFullName;

    private static String userPhoneNumber;

//    private static role userRole;

    public static String getUserFullName() {
        return userFullName;
    }

    public static String getUserPhoneNumber() {
        return userPhoneNumber;
    }

//    public static role getUserRole() {
//        return userRole;
//    }
}
