package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;

/**
 * @author Chris_Eteka
 * @since 12/29/2019
 * @email chriseteka@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachments {

    private String fileName;

    private String attachmentType;

    private String attachment;
}
