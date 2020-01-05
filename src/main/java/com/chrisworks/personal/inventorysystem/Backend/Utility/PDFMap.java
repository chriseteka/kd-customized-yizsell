package com.chrisworks.personal.inventorysystem.Backend.Utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 1/3/2020
 * @email chriseteka@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFMap {

    private String title;

    private List<String> tableHead;

    private List<?> tableData;
}
