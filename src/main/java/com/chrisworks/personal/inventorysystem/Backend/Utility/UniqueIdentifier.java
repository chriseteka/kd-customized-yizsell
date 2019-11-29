package com.chrisworks.personal.inventorysystem.Backend.Utility;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
public class UniqueIdentifier {

    public static String invoiceUID() {

        long UNIQUE_ID = (System.currentTimeMillis());

        return String.valueOf(UNIQUE_ID).substring(3, 12);
    }
}
