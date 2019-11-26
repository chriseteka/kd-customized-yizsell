package com.chrisworks.personal.inventorysystembackend.Backend.Repositories;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
