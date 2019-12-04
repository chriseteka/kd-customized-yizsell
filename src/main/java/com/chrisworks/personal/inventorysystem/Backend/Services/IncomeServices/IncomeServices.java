package com.chrisworks.personal.inventorysystem.Backend.Services.IncomeServices;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices.CRUDServices;

import java.util.Date;
import java.util.List;

public interface IncomeServices extends CRUDServices<Income> {

    List<Income> fetchAllApprovedIncome();

    List<Income> fetchAllUnApprovedIncome();

    List<Income> fetchIncomeCreatedBy(String createdBy);

    List<Income> fetchAllIncomeCreatedOn(Date createdOn);

    List<Income> fetchAlIncomeBetween(Date from, Date to);

    List<Income> fetchAllIncomeByType(int incomeTypeValue);

    List<Income> fetchAllIncomeInShop(Long shopId);

}
