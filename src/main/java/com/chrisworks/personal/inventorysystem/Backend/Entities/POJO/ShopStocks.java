package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
//For Business Owners with no warehouse, stocks are added directly into their shops, and sold from there
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ShopStocks")
public class ShopStocks extends Stock {

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "stocksInShops", joinColumns = @JoinColumn(name = "shopId"), inverseJoinColumns = @JoinColumn(name = "stockId"))
    private Shop shop;
}
