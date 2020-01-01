package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * @author Chris_Eteka
 * @since 12/26/2019
 * @email chriseteka@gmail.com
 */
@Data
public class WaybillOrder {

    @Size(min = 3, message = "Stock name must be at least three characters")
    private String stockName;

    @Min(value = 1, message = "Stock quantity must be greater than zero")
    private int quantity;
}
