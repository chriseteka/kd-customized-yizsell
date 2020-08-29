package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Shop;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/24/2019
 * @email chriseteka@gmail.com
 */
public interface ShopStocksRepository extends JpaRepository<ShopStocks, Long> {

    ShopStocks findDistinctByStockNameAndShop(String stockName, Shop shop);

    List<ShopStocks> findAllByShop(Shop shop);

    List<ShopStocks> findAllByShop_ShopId(Long shopId);

    List<ShopStocks> findAllByCreatedByAndApprovedIsFalse(String createdBy);

    ShopStocks findDistinctByStockBarCodeId(String stockBarcodeId);
}
