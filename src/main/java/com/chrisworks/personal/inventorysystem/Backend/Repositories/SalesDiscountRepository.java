package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SalesDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesDiscountRepository extends JpaRepository<SalesDiscount, Long> {

    List<SalesDiscount> findAllByCreatedBy(String createdBy);
}
