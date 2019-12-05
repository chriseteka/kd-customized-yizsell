package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chris_Eteka
 * @since 12/5/2019
 * @email chriseteka@gmail.com
 */
public interface StockCategoryRepository extends JpaRepository<StockCategory, Long> {

    StockCategory findDistinctFirstByCategoryName(String stockCategoryName);
}
