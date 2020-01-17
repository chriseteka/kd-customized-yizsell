package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.BulkUploadResponseWrapper;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.PAYMENT_MODE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Invoice;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ReturnedStock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.ShopStocks;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.ShopStockServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.futureDate;

@RestController
@RequestMapping("/shopStock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ShopStockController {

    private final ShopStockServices shopStockServices;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ShopStockController(ShopStockServices shopStockServices, ApplicationEventPublisher eventPublisher) {
        this.shopStockServices = shopStockServices;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockInShop(@RequestParam Long shopId,
                                               @RequestBody @Valid ShopStocks stock){

        ShopStocks newStockInShop = shopStockServices.createStockInShop(shopId, stock);

        if (null == newStockInShop) throw new InventoryAPIOperationException("Stock not created",
                "Stock not created successfully in shop, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "A new stock with name: " + stock.getStockName() + " has been added to your shop.",
                    APPLICATION_EVENTS.SHOP_STOCK_UP_EVENT));
        }

        return ResponseEntity.ok(newStockInShop);
    }

    @PostMapping(path = "/create/list", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockListInShop(@RequestParam Long shopId,
                                                        @RequestBody @Valid List<ShopStocks> stockList){

        if (stockList.isEmpty()) throw new InventoryAPIOperationException("Empty list of stocks",
                "You are trying to save an empty list of stock, this is not allowed", null);

        BulkUploadResponseWrapper uploadResponse = shopStockServices.createStockListInShop(shopId, stockList);

        if (null == uploadResponse)
            throw new InventoryAPIOperationException("Stock not created",
                    "Stock not created successfully in shop, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "New stock list with size: " + stockList.size() + " were uploaded to your shop.",
                    APPLICATION_EVENTS.SHOP_STOCK_UP_EVENT));
        }

        return ResponseEntity.ok(uploadResponse);
    }

    @GetMapping(path = "/all/byShop")
    public ResponseEntity<?> fetchAllByShopId(@RequestParam Long shopId, @RequestParam int page,
                                              @RequestParam int size){

        List<ShopStocks> shopStocksList = shopStockServices.allStockByShopId(shopId)
                .stream()
                .sorted(Comparator.comparing(ShopStocks::getCreatedDate).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(shopStocksList, page, size));
    }

    @GetMapping(path = "/all/soonToFinish/byShop")
    public ResponseEntity<?> fetchAllSoonToFinishInShop(@RequestParam Long shopId,
                                                  @RequestParam int limit){

        return ResponseEntity.ok(shopStockServices.allSoonToFinishStock(shopId, limit));
    }

    @GetMapping(path = "/all/soonToExpire/byShop")
    public ResponseEntity<?> fetchAllSoonToExpireInShop(@RequestParam Long shopId){

        return ResponseEntity.ok(shopStockServices.allSoonToExpireStock(shopId, futureDate(60)));
    }

    @GetMapping(path = "/all/approved/byShop")
    public ResponseEntity<?> fetchAllApprovedStockInShop(@RequestParam Long shopId){

        return ResponseEntity.ok(shopStockServices.allApprovedStock(shopId));
    }

    @GetMapping(path = "/all/unApproved/byShop")
    public ResponseEntity<?> fetchAllUnApprovedStockInShop(@RequestParam Long shopId){

        return ResponseEntity.ok(shopStockServices.allUnApprovedStock(shopId));
    }

    @GetMapping(path = "/all/unApproved/byCreator")
    public ResponseEntity<?> fetchAllUnApprovedStockCreatedBy(@RequestParam String createdBy){

        return ResponseEntity.ok(shopStockServices.allUnApprovedStockByCreator(createdBy));
    }

    @PutMapping(path = "/approve/byStockId")
    public ResponseEntity<?> approveShopStockByStockId(@RequestParam Long stockId){

        ShopStocks approvedStock = shopStockServices.approveStock(stockId);

        if (null == approvedStock) throw new InventoryAPIOperationException("Shop stock not approved",
                "Stock not approved successfully, review your inputs and try again", null);

        return ResponseEntity.ok(approvedStock);
    }

    @PutMapping(path = "/approve/byStockIdList")
    public ResponseEntity<?> approveShopStockByStockIdList(@RequestParam List<Long> stockIdList){

        return ResponseEntity.ok(shopStockServices.approveStockList(stockIdList));
    }

    @DeleteMapping(path = "/delete/byStockId")
    public ResponseEntity<?> deleteShopStockByStockId(@RequestParam Long stockId){

        ShopStocks deletedStock = shopStockServices.deleteStock(stockId);

        if (null == deletedStock) throw new InventoryAPIOperationException("Shop stock not deleted",
                "Stock with id: " + stockId + " was not deleted successfully, review your inputs and try again", null);

        return ResponseEntity.ok(deletedStock);
    }

    @PutMapping(path = "/restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> restockExistingShopStock(@RequestParam Long shopId,
                                                      @RequestParam Long stockId,
                                                      @RequestBody @Valid ShopStocks stock){

        ShopStocks reStockToShop = shopStockServices.reStockToShop(shopId, stockId, stock);

        if (null == reStockToShop) throw new InventoryAPIOperationException("Restock failed",
                "Restock failed, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Restock just occurred on a stock with name: " + stock.getStockName() +
                            " has been added to previously existing ones in your shop.",
                    APPLICATION_EVENTS.RESTOCK_EVENT));
        }

        return ResponseEntity.ok(reStockToShop);
    }

    @PutMapping(path = "/changeSellingPrice/byStockId")
    public ResponseEntity<?> changeStockSellingPriceByStockId(@RequestParam Long stockId,
                                                         @RequestParam BigDecimal newSellingPrice){

        ShopStocks shopStock = shopStockServices.changeStockSellingPriceByStockId(stockId, newSellingPrice);

        if (shopStock == null)
            throw new InventoryAPIOperationException("Data not updated",
                        "Could not change selling price for stock with value: " + newSellingPrice.toString(), null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Selling price of stock in your shop with name: " + shopStock.getStockName() +
                            " has been changed to: " + newSellingPrice,
                    APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
        }

        return ResponseEntity.ok(shopStock);
    }

    @PutMapping(path = "/changeSellingPrice/byStockName")
    public ResponseEntity<?> changeStockSellingPriceByName(@RequestParam Long shopId,
                                                           @RequestParam String stockName,
                                                           @RequestParam BigDecimal newSellingPrice){

        ShopStocks shopStock = shopStockServices
                .changeStockSellingPriceByShopIdAndStockName(shopId, stockName, newSellingPrice);

        if (shopStock == null)
            throw new InventoryAPIOperationException("Data not updated",
                        "Could not change selling price for stock with value: " + newSellingPrice.toString(), null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "Selling price of stock in your shop with name: " + stockName +
                            " has been changed to: " + newSellingPrice,
                    APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
        }

        return ResponseEntity.ok(shopStock);
    }

    @PostMapping(path = "/sell", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sellStock(@RequestParam Long shopId,
                                       @RequestBody @Valid Invoice invoice){

        if (!invoice.getPaymentModeVal().matches("\\d+")) throw new InventoryAPIDataValidationException
                ("Payment mode value error", "Payment mode value must be any of these: 100, 200, 300", null);

        invoice.setPaymentModeValue(Integer.parseInt(invoice.getPaymentModeVal()));

        IntStream paymentModeValueStream = Arrays.stream(PAYMENT_MODE.values()).mapToInt(PAYMENT_MODE::getPayment_mode_value);

        if (paymentModeValueStream.noneMatch(value -> value == invoice.getPaymentModeValue()))
            throw new InventoryAPIDataValidationException("Payment mode value error", "Payment mode value must be any of these: 100, 200, 300", null);

        Invoice newInvoice;

        try {
            newInvoice = shopStockServices.sellStock(shopId, invoice);
        }catch (Exception e){
            throw new InventoryAPIOperationException(e.getLocalizedMessage(), e.getMessage(), null);
        }

        if (newInvoice == null)
            throw new InventoryAPIOperationException("Data not saved",
                    "Could not complete stock sale successfully, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "A sale has been made in your shop with invoice number: " + newInvoice.getInvoiceNumber()
                    + ", invoice amounts to: " + newInvoice.getInvoiceTotalAmount() + ", amount paid: " + newInvoice.getAmountPaid(),
                    APPLICATION_EVENTS.SALE_EVENT));
        }

        return ResponseEntity.ok(newInvoice);
    }

    @PostMapping(path = "/returnStock/toShop", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturn(@RequestParam Long shopId,
                                           @RequestBody @Valid ReturnedStock returnedStock){

        ReturnedStock newReturnedStock;

        try {
            newReturnedStock = shopStockServices.processReturn(shopId, returnedStock);
        }catch(Exception e){
            throw new InventoryAPIOperationException(e.getLocalizedMessage(), e.getMessage(), null);
        }

        if (newReturnedStock == null)
            throw new InventoryAPIOperationException("Data not saved",
                    "Stock not returned successfully, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    "A return has been made in your shop with invoice number: " + newReturnedStock.getInvoiceId()
                    + ", name of stock returned: " + newReturnedStock.getStockName()
                            + ", worth of the stock returned is: " + newReturnedStock.getStockReturnedCost(),
                    APPLICATION_EVENTS.RETURN_SALE_EVENT));
        }

        return ResponseEntity.ok(newReturnedStock);
    }

    @PostMapping(path = "/returnStockList/toShop", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processReturnList(@RequestParam Long shopId,
                                               @RequestBody @Valid List<ReturnedStock> returnedStockList){

        List<ReturnedStock> newReturnedStockList = shopStockServices.processReturnList(shopId, returnedStockList);

        if (newReturnedStockList == null || newReturnedStockList.isEmpty()) throw new InventoryAPIOperationException
                ("List not saved", "Stock list not returned successfully, review your inputs and try again.", null);

        return ResponseEntity.ok(newReturnedStockList);
    }
}