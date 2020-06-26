package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
//This object is created on successful authentication, and they form the requirements(claims) for jwt
@Data
@AllArgsConstructor
public class AuthenticatedUserDetails {

    private static Long userId;

    private static String userFullName;

    private static ACCOUNT_TYPE account_type;

    private static Boolean hasWarehouse = false;

    private static String businessId;

    public static String getUserFullName() {
        return userFullName;
    }

    public static Long getUserId() {
        return userId;
    }

    public static ACCOUNT_TYPE getAccount_type(){ return account_type; }

    public static Boolean getHasWarehouse(){ return hasWarehouse; }

    public static String getBusinessId(){
        return businessId;
    }

    public AuthenticatedUserDetails(Long userId, String fullName, ACCOUNT_TYPE account_type, Boolean hasWarehouse,
                                    String businessId) {

        AuthenticatedUserDetails.userId = userId;
        AuthenticatedUserDetails.userFullName = fullName;
        AuthenticatedUserDetails.account_type = account_type;
        AuthenticatedUserDetails.hasWarehouse = hasWarehouse;
        AuthenticatedUserDetails.businessId = businessId;
    }
}
