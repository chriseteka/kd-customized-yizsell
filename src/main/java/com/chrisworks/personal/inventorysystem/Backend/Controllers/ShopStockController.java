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
import com.chrisworks.personal.inventorysystem.Backend.Websocket.controllers.WebsocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatMoney;
import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.futureDate;

@RestController
@RequestMapping("/shopStock")
public class ShopStockController {

    private final ShopStockServices shopStockServices;
    private final WebsocketController websocketController;
    private final ApplicationEventPublisher eventPublisher;
    private String description = null;

    @Autowired
    public ShopStockController(ShopStockServices shopStockServices, WebsocketController websocketController,
                               ApplicationEventPublisher eventPublisher) {
        this.shopStockServices = shopStockServices;
        this.websocketController = websocketController;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createStockInShop(@RequestParam Long shopId,
                                               @RequestBody @Valid ShopStocks stock){

        ShopStocks newStockInShop = shopStockServices.createStockInShop(shopId, stock);

        if (null == newStockInShop) throw new InventoryAPIOperationException("Stock not created",
                "Stock not created successfully in shop, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A new stock with name: " + stock.getStockName() + " has been added to your shop.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.SHOP_STOCK_UP_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
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

            description = "New stock list with size: " + stockList.size() + " were uploaded to your shop.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.SHOP_STOCK_UP_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(uploadResponse);
    }

    @GetMapping(path = "/all/byShop")
    public ResponseEntity<?> fetchAllByShopId(@RequestParam Long shopId, @RequestParam int page, @RequestParam int size,
                                              @RequestParam(required = false, defaultValue = "") String search){

        List<ShopStocks> shopStocksList = shopStockServices.allStockByShopId(shopId)
                .stream().filter(stock -> {
                    if (!StringUtils.hasText(search)) return true;
                    final String searchQuery = search.toLowerCase();
                    return stock.getStockName().toLowerCase().contains(searchQuery)
                            || String.valueOf(stock.getStockQuantityRemaining()).contains(searchQuery)
                            || String.valueOf(stock.getStockQuantitySold()).contains(searchQuery);
                })
                .sorted(Comparator.comparing(ShopStocks::getCreatedDate)
                    .thenComparing(ShopStocks::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(shopStocksList, page, size));
    }

    @GetMapping(path = "/all/unique/byShop")
    public ResponseEntity<?> fetchAllUniqueStockByWarehouseId(@RequestParam Long shopId){

        return ResponseEntity.ok(shopStockServices.fetchAllAuthUserUniqueStocks(shopId));
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
    public ResponseEntity<?> deleteShopStockByStockId(@RequestParam Long shopId,
                                                      @RequestParam Long... stockId){

        return ResponseEntity.ok(shopStockServices.deleteStock(shopId, stockId));
    }

    @PutMapping(path = "/restock", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> restockExistingShopStock(@RequestParam Long shopId,
                                                      @RequestParam Long stockId,
                                                      @RequestBody @Valid ShopStocks stock){

        ShopStocks reStockToShop = shopStockServices.reStockToShop(shopId, stockId, stock);

        if (null == reStockToShop) throw new InventoryAPIOperationException("Restock failed",
                "Restock failed, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "Restock just occurred on a stock with name: " + stock.getStockName() +
                    " has been added to previously existing ones in your shop.";
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.RESTOCK_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
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

            description = "Selling price of stock in your shop has been changed, details: "
                    + "\nName: " + shopStock.getStockName()
                    + "\nNew Selling price: " + formatMoney(newSellingPrice);
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
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

            description = "Selling price of stock in your shop has been changed, details: "
                    + "\nName: " + stockName
                    + "\nNew Selling price: " + formatMoney(newSellingPrice);
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.SELLING_PRICE_CHANGED_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(shopStock);
    }

    @PostMapping(path = "/sell", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sellStock(@RequestParam Long shopId,
                                       @RequestBody @Valid Invoice invoice){

        if (!invoice.getPaymentModeVal().matches("\\d+")) throw new InventoryAPIDataValidationException
                ("Payment mode value error", "Payment mode value must be any of these: 100, 200, 300, 400", null);

        invoice.setPaymentModeValue(Integer.parseInt(invoice.getPaymentModeVal()));

        IntStream paymentModeValueStream = Arrays.stream(PAYMENT_MODE.values()).mapToInt(PAYMENT_MODE::getPayment_mode_value);

        if (paymentModeValueStream.noneMatch(value -> value == invoice.getPaymentModeValue()))
            throw new InventoryAPIDataValidationException("Payment mode value error",
                    "Payment mode value must be any of these: 100, 200, 300, 400", null);

        Invoice newInvoice = shopStockServices.sellStock(shopId, invoice);

        if (newInvoice == null)
            throw new InventoryAPIOperationException("Data not saved",
                    "Could not complete stock sale successfully, review your inputs and try again", null);

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)) {

            description = "A sale has been made in your shop, details: "
                    + "\nInvoice Number: " + newInvoice.getInvoiceNumber()
                    + "\nInvoice Amounts: " + formatMoney(newInvoice.getInvoiceTotalAmount())
                    + "\nAmount Paid: " + formatMoney(newInvoice.getAmountPaid());
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.SALE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
        }

        return ResponseEntity.ok(newInvoice);
    }

    @PutMapping(path = "/reverseSale", produces = "application/json")
    public ResponseEntity<?> reverseSale(@RequestParam Long shopId,
                                         @RequestParam String invoiceNumber){

        return ResponseEntity.ok(shopStockServices.reverseSale(shopId, invoiceNumber));
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

            description = "A return has been made in your shop, details: "
                    + "\nInvoice Number: " + newReturnedStock.getInvoiceId()
                    + "\nName of stock returned: " + newReturnedStock.getStockName()
                    + "\nworth of the stock returned is: " + formatMoney(newReturnedStock.getStockReturnedCost());
            eventPublisher.publishEvent(new SellerTriggeredEvent(AuthenticatedUserDetails.getUserFullName(),
                    description, APPLICATION_EVENTS.RETURN_SALE_EVENT));
            websocketController.sendNoticeToUser(description, websocketController.businessOwnerMail());
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

    @PutMapping(path = "/change/stockQuantity", produces = "application/json")
    public ResponseEntity<?> changeStockQuantity(@RequestParam Long stockId,
                                                 @RequestParam int newQuantity){

        return ResponseEntity.ok(shopStockServices.forceChangeStockQuantity(stockId, newQuantity));
    }
}