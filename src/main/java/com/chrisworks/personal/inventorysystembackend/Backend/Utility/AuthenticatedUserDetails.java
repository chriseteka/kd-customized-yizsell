package com.chrisworks.personal.inventorysystembackend.Backend.Utility;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserDetails {

    private static Long userId;

    private static String userFullName;

    private static String userPhoneNumber;

//    private static role userRole;

    public static Long getUserId() {
        return userId;
    }

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
