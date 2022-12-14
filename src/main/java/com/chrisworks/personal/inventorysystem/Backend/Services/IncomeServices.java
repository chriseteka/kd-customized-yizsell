package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;

import java.util.Date;
import java.util.List;

public interface IncomeServices extends CRUDServices<Income> {

    List<Income> fetchAllApprovedIncome();

    List<Income> fetchAllUnApprovedIncomeByCreator(String createdBy);

    List<Income> fetchIncomeCreatedBy(String createdBy);

    List<Income> fetchAllIncomeCreatedOn(Date createdOn);

    List<Income> fetchAllIncomeBetween(Date from, Date to);

    List<Income> fetchAllIncomeByType(int incomeTypeValue);

    List<Income> fetchAllIncomeInShop(Long shopId);

    List<Income> approveIncome(Long... incomeId);

    List<Income> fetchAllUnApprovedIncome();

    List<Income> fetchAllByDescriptionContains(String description);

    List<Income> deleteIncome(Long... incomeIds);
}
