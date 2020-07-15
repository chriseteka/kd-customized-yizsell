package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

import static ir.cafebabe.math.utils.BigDecimalUtils.is;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Setter
@Getter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "procuredStocks")
public class ProcuredStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String stockName;

    private int quantity;

    private BigDecimal pricePerStockPurchased = BigDecimal.ZERO;

    private BigDecimal stockTotalPrice = BigDecimal.ZERO;

    public boolean equals(ProcuredStock ps){
        return this.stockName.equalsIgnoreCase(ps.getStockName())
                && this.quantity == ps.getQuantity()
                && is(this.pricePerStockPurchased).eq(ps.getPricePerStockPurchased())
                && is(this.stockTotalPrice).eq(ps.stockTotalPrice);
    }
}
