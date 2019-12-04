package com.chrisworks.personal.inventorysystem.Backend.Repositories;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findAllByCreatedDateIsBetween(Date from, Date to);

    List<Income> findAllByCreatedBy(String createdBy);

    List<Income> findAllByCreatedDate(Date createdDate);

    List<Income> findAllByApprovedTrue();

    List<Income> findAllByApprovedFalse();

    List<Income> findAllByIncomeTypeValue(int incomeTypeValue);
}
