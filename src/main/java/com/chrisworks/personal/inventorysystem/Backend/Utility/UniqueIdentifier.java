package com.chrisworks.personal.inventorysystem.Backend.Utility;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
public class UniqueIdentifier {

    public static String invoiceUID() {

        long UNIQUE_ID = (System.currentTimeMillis());

        //format invoice to take the form "001-4576282"
        return "00" + AuthenticatedUserDetails.getUserId() + "-" + String.valueOf(UNIQUE_ID).substring(3, 12);
    }

    public static String waybillInvoiceUID() {

        long UNIQUE_ID = (System.currentTimeMillis());

        //format invoice to take the form "WB1-4576282"
        return "WB" + AuthenticatedUserDetails.getUserId() + "-" + String.valueOf(UNIQUE_ID).substring(3, 12);
    }
}
