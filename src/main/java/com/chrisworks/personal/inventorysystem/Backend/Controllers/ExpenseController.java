package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.ExpenseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ExpenseController {

    private final ExpenseServices expenseServices;

    @Autowired
    public ExpenseController(ExpenseServices expenseServices) {
        this.expenseServices = expenseServices;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createExpense(@RequestBody @Valid Expense expense){

        Expense expenseCreated = expenseServices.createEntity(expense);

        if (null == expenseCreated)throw new InventoryAPIOperationException("Expense not saved",
                "Expense was not saved successfully, please try again", null);

        return ResponseEntity.ok(expenseCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateExpense(@RequestParam Long expenseId,
                                           @RequestBody @Valid Expense expense){

        Expense updatedExpense = expenseServices.updateEntity(expenseId, expense);

        if (null == updatedExpense) throw new InventoryAPIOperationException("Expense not updated",
                "Expense was not updated successfully, please try again", null);

        return ResponseEntity.ok(updatedExpense);
    }

    @GetMapping(path = "/byId")
    public ResponseEntity<?> getExpenseById(@RequestParam Long expenseId){

        Expense expense = expenseServices.getSingleEntity(expenseId);

        if (null == expense) throw new InventoryAPIResourceNotFoundException("Expense not found",
                "Expense with id: " + expenseId + " was not found", null);

        return ResponseEntity.ok(expense);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> getAllExpenses(){

        return ResponseEntity.ok(expenseServices.getEntityList());
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteExpenseById(@RequestParam Long expenseId){

        Expense expense = expenseServices.deleteEntity(expenseId);

        if (null == expense) throw new InventoryAPIResourceNotFoundException("Expense not deleted",
                "Expense with id: " + expenseId + " was not deleted.", null);

        return ResponseEntity.ok(expense);
    }

    @GetMapping(path = "/all/approved")
    public ResponseEntity<?> getAllApprovedExpenses(){

        return ResponseEntity.ok(expenseServices.fetchAllApprovedExpenses());
    }

    @GetMapping(path = "/all/unapproved/byCreator")
    public ResponseEntity<?> getAllUnApprovedExpensesByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(expenseServices.fetchAllUnApprovedExpensesCreatedBy(createdBy));
    }

    @GetMapping(path = "/all/byCreator")
    public ResponseEntity<?> getAllExpensesByCreator(@RequestParam String createdBy){

        return ResponseEntity.ok(expenseServices.fetchExpensesCreatedBy(createdBy));
    }

    @GetMapping(path = "/all/byCreatedDate")
    public ResponseEntity<?> getAllExpensesByCreatedDate(@RequestParam Date createdDate){

        return ResponseEntity.ok(expenseServices.fetchAllExpensesCreatedOn(createdDate));
    }

    @GetMapping(path = "/all/byCreatedDateBetween")
    public ResponseEntity<?> getAllExpensesBetween(@RequestParam Date from,
                                                   @RequestParam Date to){

        return ResponseEntity.ok(expenseServices.fetchAllExpensesBetween(from, to));
    }

    @GetMapping(path = "/all/byType")
    public ResponseEntity<?> getAllExpensesByType(@RequestParam int expenseType){

        return ResponseEntity.ok(expenseServices.fetchAllExpensesByType(expenseType));
    }

    @GetMapping(path = "/all/byShop")
    public ResponseEntity<?> getAllExpensesByShop(@RequestParam Long shopId){

        return ResponseEntity.ok(expenseServices.fetchAllExpensesInShop(shopId));
    }

    @PutMapping(path = "/approve/expense")
    public ResponseEntity<?> approveExpense(@RequestParam Long expenseId){

        Expense approvedExpense = expenseServices.approveExpense(expenseId);

        if (null == approvedExpense) throw new InventoryAPIOperationException
                ("Expense not approved", "Expense not approved, please try again.", null);

        return ResponseEntity.ok(approvedExpense);
    }

    @GetMapping(path = "/all/unapproved")
    public ResponseEntity<?> getAllUnApprovedExpenses(){

        return ResponseEntity.ok(expenseServices.fetchAllUnApprovedExpense());
    }
}
