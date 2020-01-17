package com.chrisworks.personal.inventorysystem.Backend.Entities;

import lombok.Data;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 1/17/2020
 * @email chriseteka@gmail.com
 */
@Data
public class BulkUploadResponseWrapper {

    private List<?> successfulUploads;

    private List<?> failedUploads;

    public static BulkUploadResponseWrapper bulkUploadResponse(List<?> successfulUploads, List<?> failedUploads) {

        BulkUploadResponseWrapper b = new BulkUploadResponseWrapper();

        b.setSuccessfulUploads(successfulUploads);
        b.setFailedUploads(failedUploads);

        return b;
    }
}
