package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.IncomeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/income")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class IncomeController {

    private final IncomeServices incomeServices;

    @Autowired
    public IncomeController(IncomeServices incomeServices) {
        this.incomeServices = incomeServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createIncome(@RequestBody @Valid Income income){

        Income incomeCreated = incomeServices.createEntity(income);

        if (null == incomeCreated)throw new InventoryAPIOperationException("Income not saved",
                "Income was not saved successfully, please try again", null);

        return ResponseEntity.ok(incomeCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateIncome(@RequestParam Long incomeId,
                                           @RequestBody @Valid Income income){

        Income updatedIncome = incomeServices.updateEntity(incomeId, income);

        if (null == updatedIncome) throw new InventoryAPIOperationException("Income not updated",
                "Income was not updated successfully, please try again", null);

        return ResponseEntity.ok(updatedIncome);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> getIncomeById(@RequestParam Long incomeId){

        Income income = incomeServices.getSingleEntity(incomeId);

        if (null == income) throw new InventoryAPIResourceNotFoundException("Income not found",
                "Income with id: " + incomeId + " was not found", null);

        return ResponseEntity.ok(income);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> getAllIncomes(){

        return ResponseEntity.ok(incomeServices.getEntityList());
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteIncomeById(@RequestParam Long incomeId){

        Income income = incomeServices.deleteEntity(incomeId);

        if (null == income) throw new InventoryAPIResourceNotFoundException("Income not deleted",
                "Income with id: " + incomeId + " was not deleted.", null);

        return ResponseEntity.ok(income);
    }

    @GetMapping(path = "/all/approved")
    public ResponseEntity<?> getAllApprovedIncomes(){

        return ResponseEntity.ok(incomeServices.fetchAllApprovedIncome());
    }

    @GetMapping(path = "/all/unapproved/byCreator")
    public ResponseEntity<?> getAllUnApprovedIncomesByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(incomeServices.fetchAllUnApprovedIncomeByCreator(createdBy));
    }

    @GetMapping(path = "/all/byCreator")
    public ResponseEntity<?> getAllIncomesByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(incomeServices.fetchIncomeCreatedBy(createdBy));
    }

    @GetMapping(path = "/all/byCreatedDate")
    public ResponseEntity<?> getAllIncomesByCreatedDate(@RequestParam Date createdDate){

        return ResponseEntity.ok(incomeServices.fetchAllIncomeCreatedOn(createdDate));
    }

    @GetMapping(path = "/all/byCreatedDateBetween")
    public ResponseEntity<?> getAllIncomesBetween(@RequestParam Date from,
                                                   @RequestParam Date to){

        return ResponseEntity.ok(incomeServices.fetchAllIncomeBetween(from, to));
    }

    @GetMapping(path = "/all/byType")
    public ResponseEntity<?> getAllIncomesByType(@RequestParam int incomeType){

        return ResponseEntity.ok(incomeServices.fetchAllIncomeByType(incomeType));
    }

    @GetMapping(path = "/all/byShop")
    public ResponseEntity<?> getAllIncomesByShop(@RequestParam Long shopId){

        return ResponseEntity.ok(incomeServices.fetchAllIncomeInShop(shopId));
    }

    @PutMapping(path = "/approve/income")
    public ResponseEntity<?> approveIncome(@RequestParam Long incomeId){

        Income approvedIncome = incomeServices.approveIncome(incomeId);

        if (null == approvedIncome) throw new InventoryAPIOperationException
                ("Income not approved", "Income not approved, please try again.", null);

        return ResponseEntity.ok(approvedIncome);
    }

    @GetMapping(path = "/all/unapproved")
    public ResponseEntity<?> getAllUnApprovedIncomes(){

        return ResponseEntity.ok(incomeServices.fetchAllUnApprovedIncome());
    }

}
