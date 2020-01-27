package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.DiscountModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountModelRepository extends JpaRepository<DiscountModel, Long> {

    DiscountModel findDistinctByDiscountNameAndCreatedBy(String discountName, String createdBy);

    List<DiscountModel> findAllByCreatedBy(String createdBy);
}
