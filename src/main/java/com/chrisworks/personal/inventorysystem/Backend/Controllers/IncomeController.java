package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.INCOME_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Income;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Services.IncomeServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers.WebsocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatMoney;

@RestController
@RequestMapping("/income")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class IncomeController {

    private final IncomeServices incomeServices;
    private final WebsocketController websocketController;
    private final ApplicationEventPublisher eventPublisher;
    private String description = null;

    @Autowired
    public IncomeController(IncomeServices incomeServices, WebsocketController websocketController,
                            ApplicationEventPublisher eventPublisher) {
        this.incomeServices = incomeServices;
        this.websocketController = websocketController;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createIncome(@RequestBody @Valid Income income){

        if (!income.getIncomeTypeVal().matches("\\d+"))
            throw new InventoryAPIDataValidationException("Income Type value error",
                    "Income Type value must be any of these: 100, 200, 300", null);

        income.setIncomeTypeValue(Integer.parseInt(income.getIncomeTypeVal()));

        IntStream incomeValueStream = Arrays.stream(INCOME_TYPE.values()).mapToInt(INCOME_TYPE::getIncome_type_value);

        if (incomeValueStream.noneMatch(value -> value == income.getIncomeTypeValue())) throw new
                InventoryAPIDataValidationException("Income Type value error",
                "Income Type value must be any of these: 100, 200, 300", null);

        Income incomeCreated = incomeServices.createEntity(income);

        if (null == incomeCreated)throw new InventoryAPIOperationException("Income not saved",
                "Income was not saved successfully, please try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A new income has been created, details:"
                    + "\namount: " + formatMoney(income.getIncomeAmount())
                    + "\nreview and approve this income as soon as possible.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.INCOME_CREATE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(incomeCreated);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateIncome(@RequestParam Long incomeId,
                                           @RequestBody @Valid Income income){

        Income updatedIncome = incomeServices.updateEntity(incomeId, income);

        if (null == updatedIncome) throw new InventoryAPIOperationException("Income not updated",
                "Income was not updated successfully, please try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "An income has been updated, details:"
                    + "\namount: " + formatMoney(income.getIncomeAmount())
                    + "\nreview and approve this income as soon as possible.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.INCOME_UPDATE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

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
    public ResponseEntity<?> getAllIncomes(@RequestParam int page, @RequestParam int size,
                                           @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedIncomeList(incomeServices.getEntityList(), search, page, size));
    }

    @DeleteMapping(path = "/byId")
    public ResponseEntity<?> deleteIncomeById(@RequestParam Long[] incomeId){

        return ResponseEntity.ok(incomeServices.deleteIncome(incomeId));
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
    public ResponseEntity<?> getAllIncomesByShop(@RequestParam Long shopId, @RequestParam int page, @RequestParam int size,
                                                 @RequestParam(required = false, defaultValue = "") String search){

        return ResponseEntity.ok(paginatedIncomeList(incomeServices.fetchAllIncomeInShop(shopId), search, page, size));
    }

    @PutMapping(path = "/approve/income")
    public ResponseEntity<?> approveIncome(@RequestParam Long[] incomeId){

        return ResponseEntity.ok(incomeServices.approveIncome(incomeId));
    }

    @GetMapping(path = "/all/unapproved")
    public ResponseEntity<?> getAllUnApprovedIncomes(){

        return ResponseEntity.ok(incomeServices.fetchAllUnApprovedIncome());
    }

    private ListWrapper paginatedIncomeList(List<Income> incomeList, String search, int page, int size){

        return prepareResponse(incomeList.stream()
            .filter(income -> {
                if (!StringUtils.hasText(search)) return true;
                return income.getCreatedBy().contains(search.toLowerCase())
                        || String.valueOf(income.getIncomeAmount()).contains(search)
                        || String.valueOf(income.getIncomeType()).contains(search.toUpperCase());
            }).sorted(Comparator.comparing(Income::getCreatedDate)
                .thenComparing(Income::getCreatedTime).reversed())
            .collect(Collectors.toList()), page, size);
    }

}
