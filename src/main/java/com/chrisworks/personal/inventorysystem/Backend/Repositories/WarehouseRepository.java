package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Warehouse findDistinctByWarehouseNameAndCreatedBy(String warehouseName, String createdBy);

    List<Warehouse> findAllByCreatedBy(String createdBy);
}
