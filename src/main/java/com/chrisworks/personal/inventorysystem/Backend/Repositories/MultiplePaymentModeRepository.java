package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.MultiplePaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultiplePaymentModeRepository extends JpaRepository<MultiplePaymentMode, Long> {
}
