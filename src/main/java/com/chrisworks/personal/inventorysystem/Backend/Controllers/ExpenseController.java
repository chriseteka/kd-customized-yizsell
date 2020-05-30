package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EXPENSE_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Expense;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.ExpenseServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers.WebsocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatMoney;

@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ExpenseController {

    private final ExpenseServices expenseServices;
    private final WebsocketController websocketController;
    private final ApplicationEventPublisher eventPublisher;
    private String description = null;

    @Autowired
    public ExpenseController(ExpenseServices expenseServices, WebsocketController websocketController,
                             ApplicationEventPublisher eventPublisher) {
        this.expenseServices = expenseServices;
        this.websocketController = websocketController;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createExpense(@RequestBody @Valid Expense expense){

        if (!expense.getExpenseTypeVal().matches("\\d+")) throw new InventoryAPIDataValidationException
                ("Expense Type value error", "Expense Type value must be any of these: 100, 200, 300, 400, 500", null);

        expense.setExpenseTypeValue(Integer.parseInt(expense.getExpenseTypeVal()));

        IntStream expenseValueStream = Arrays.stream(EXPENSE_TYPE.values()).mapToInt(EXPENSE_TYPE::getExpense_type_value);

        if (expenseValueStream.noneMatch(value -> value == expense.getExpenseTypeValue()))
            throw new InventoryAPIDataValidationException("Expense Type value error",
                    "Expense Type value must be any of these: 100, 200, 300, 400, 500", null);

        Expense expenseCreated = expenseServices.createEntity(expense);

        if (null == expenseCreated)throw new InventoryAPIOperationException("Expense not saved",
                "Expense was not saved successfully, please try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A new expense has been created, details:"
                    + "\namount: " + formatMoney(expense.getExpenseAmount())
                    + "\nreview and approve this expense as soon as possible.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.EXPENSE_CREATE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(expenseCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateExpense(@RequestParam Long expenseId,
                                           @RequestBody @Valid Expense expense){

        Expense updatedExpense = expenseServices.updateEntity(expenseId, expense);

        if (null == updatedExpense) throw new InventoryAPIOperationException("Expense not updated",
                "Expense was not updated successfully, please try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "An expense has been updated, details:"
                    + "\namount: " + formatMoney(expense.getExpenseAmount())
                    + "\nreview and approve this expense as soon as possible.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.EXPENSE_UPDATE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

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
    public ResponseEntity<?> getAllExpenses(@RequestParam int page, @RequestParam int size){

        List<Expense> expenseList = expenseServices.getEntityList()
                .stream()
                .sorted(Comparator.comparing(Expense::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(expenseList, page, size));
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
