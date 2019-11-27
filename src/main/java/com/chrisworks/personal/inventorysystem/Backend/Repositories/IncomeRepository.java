package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface IncomeRepository extends JpaRepository<Income, Long> {
}
