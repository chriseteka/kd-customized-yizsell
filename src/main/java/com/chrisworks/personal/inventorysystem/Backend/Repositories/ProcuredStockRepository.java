package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ProcuredStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Repository
public interface ProcuredStockRepository extends JpaRepository<ProcuredStock, Long> {
}
