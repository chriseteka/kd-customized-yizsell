package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Procurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@Repository
public interface ProcurementRepository extends JpaRepository<Procurement, Long> {

    List<Procurement> findAllByCreatedBy(String createdBy);

    Optional<Procurement> findDistinctByWaybillIdAndCreatedBy(String waybillId, String createdBy);
}
