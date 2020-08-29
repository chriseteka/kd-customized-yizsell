package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.DesktopPushObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.models.UserMiniProfile;
import com.chrisworks.personal.inventorysystem.Backend.Websocket.repoServices.UserMiniProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;


/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class ShopServicesImpl implements ShopServices {

    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ReturnedStockRepository returnedStockRepository;
    private final InvoiceRepository invoiceRepository;
    private final ShopStocksRepository shopStocksRepository;
    private final SellerRepository sellerRepository;
    private final UserMiniProfileRepository userMiniProfileRepository;
    private final StockSoldRepository stockSoldRepository;
    private final MultiplePaymentModeRepository multiplePaymentModeRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private Map<String, Customer> customerMap;


    @Autowired
    public ShopServicesImpl(ShopRepository shopRepository, BusinessOwnerRepository businessOwnerRepository,
                            CustomerRepository customerRepository, ExpenseRepository expenseRepository,
                            IncomeRepository incomeRepository, ReturnedStockRepository returnedStockRepository,
                            InvoiceRepository invoiceRepository, ShopStocksRepository shopStocksRepository,
                            SellerRepository sellerRepository, UserMiniProfileRepository userMiniProfileRepository,
                            StockSoldRepository stockSoldRepository, MultiplePaymentModeRepository multiplePaymentModeRepository) {
        this.shopRepository = shopRepository;
        this.businessOwnerRepository = businessOwnerRepository;
        this.customerRepository = customerRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.returnedStockRepository = returnedStockRepository;
        this.invoiceRepository = invoiceRepository;
        this.shopStocksRepository = shopStocksRepository;
        this.sellerRepository = sellerRepository;
        this.userMiniProfileRepository = userMiniProfileRepository;
        this.stockSoldRepository = stockSoldRepository;
        this.multiplePaymentModeRepository = multiplePaymentModeRepository;
    }

    @Override
    public Shop createShop(Long businessOwnerId, Shop shop) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        Optional<BusinessOwner> businessOwner = businessOwnerRepository.findById(businessOwnerId);

        if (!businessOwner.isPresent()) throw new InventoryAPIOperationException
                ("Unknown user", "Could not detect the user trying to create a new shop", null);

        verifyShopCreationLimitViolation(businessOwner.get());

        if (shopRepository.findDistinctByShopNameAndCreatedBy(shop.getShopName(),
                AuthenticatedUserDetails.getUserFullName()) != null) throw new InventoryAPIOperationException
                ("Shop name already exist", "A shop already exist with the name: " + shop.getShopName(), null);

        shop.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
        shop.setBusinessOwner(businessOwner.get());
        return shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findById(shopId)
                .map(shop -> {

                    if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                        throw new InventoryAPIOperationException
                                ("shop is not yours", "This shop was not created by you", null);

                    return shop;
                }).orElse(null);
    }

    @Override
    public Shop updateShop(Long shopId, Shop shopUpdates) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findById(shopId).map(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException
                        ("shop is not yours", "This shop was not created by you", null);

            shop.setUpdateDate(new Date());
            shop.setShopName(shopUpdates.getShopName());
            shop.setShopAddress(shopUpdates.getShopAddress());
            return shopRepository.save(shop);
        }).orElse(null);
    }

    @Override
    public List<Shop> fetchAllShops() {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return shopRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
    }

    @Override
    public Shop deleteShop(Long shopId) {

        return shopRepository.findById(shopId).map(shop -> {

            if (!shop.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your seller", "Seller not created by you", null);

            shopRepository.delete(shop);
            return shop;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Shop not found",
                "Shop with id: " + shopId + " was not found", null));
    }

    @Override
    public List<Shop> deleteShops(Long... shopIds) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Long> shopIdsToDelete = Arrays.asList(shopIds);
        if (shopIdsToDelete.size() == 1)
            return Collections.singletonList(deleteShop(shopIdsToDelete.get(0)));

        List<Shop> shopsToDelete = fetchAllShops().stream()
                .filter(shop -> shopIdsToDelete.contains(shop.getShopId()))
                .collect(Collectors.toList());

        if (!shopsToDelete.isEmpty()) shopRepository.deleteAll(shopsToDelete);

        return shopsToDelete;
    }

    @Override
    @Transactional
    public ResponseObject receiveDesktopPush(DesktopPushObject desktopPushObject) {

        List<Customer> customers = Collections.emptyList();
        List<Expense> expenses = Collections.emptyList();
        List<Income> incomes = Collections.emptyList();
        List<ReturnedStock> returnedStocks = Collections.emptyList();
        List<Invoice> invoices = Collections.emptyList();
        List<ShopStocks> shopStocks = Collections.emptyList();
        boolean status;

        Shop shop = shopRepository.findById(desktopPushObject.getShopId())
                .orElseThrow(() -> new InventoryAPIResourceNotFoundException("Shop not found", "Shop not found", null));

        List<Seller> sellerList = sellerRepository.findAllByCreatedBy(shop.getCreatedBy());
        Seller authSeller = sellerList.stream().filter(seller -> seller.getSellerEmail()
                .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())).collect(toSingleton());
        UserMiniProfile authUserMiniProfile = userMiniProfileRepository.findDistinctByEmail(authSeller.getSellerEmail());
        List<String> sellers = sellerList.stream().map(Seller::getSellerEmail).collect(Collectors.toList());
        sellers.add(shop.getCreatedBy());
        readyCustomerMap(sellers);

        //Settled
        if (desktopPushObject.getCustomers() != null && !desktopPushObject.getCustomers().isEmpty()) {
            List<Customer> customerList = new ArrayList<>();
            desktopPushObject.getCustomers().forEach(customer -> {
                customer.setCustomerId(null);
                if (customerMap.containsKey(customer.getCustomerPhoneNumber()))
                    customer.setCustomerId(customerMap.get(customer.getCustomerPhoneNumber()).getCustomerId());
                customerList.add(customer);
            });
           customers = customerRepository.saveAll(customerList);
        }

        customerMap.clear();
        readyCustomerMap(sellers);

        //Settled
        if (desktopPushObject.getExpenses() != null && !desktopPushObject.getExpenses().isEmpty())
            expenses = expenseRepository.saveAll(desktopPushObject.getExpenses().stream()
                .peek(e -> {
                    e.setExpenseId(null);
                    e.setShop(shop);
                    e.setExpenseTypeVal(String.valueOf(e.getExpenseTypeValue()));
                }).collect(Collectors.toList()));

        //Settled
        if (desktopPushObject.getIncomes() != null && !desktopPushObject.getIncomes().isEmpty())
            incomes = incomeRepository.saveAll(desktopPushObject.getIncomes().stream()
                .peek(e -> {
                    e.setIncomeId(null);
                    e.setShop(shop);
                    e.setIncomeTypeVal(String.valueOf(e.getIncomeTypeValue()));
                }).collect(Collectors.toList()));

        //Settled
        if (desktopPushObject.getReturnedStocks() != null && !desktopPushObject.getReturnedStocks().isEmpty())
            returnedStocks = returnedStockRepository.saveAll(desktopPushObject.getReturnedStocks().stream()
                .peek(e -> {
                    e.setReturnedStockId(null);
                    e.setShop(shop);
                    if (e.getCustomerId() != null)
                        e.setCustomerId(customerMap.get(e.getCustomerId().getCustomerPhoneNumber()));
                }).collect(Collectors.toList()));

        //Settled
        if (desktopPushObject.getInvoices() != null && !desktopPushObject.getInvoices().isEmpty())
            invoices = invoiceRepository.saveAll(desktopPushObject.getInvoices().stream()
                .peek(e -> {
                    e.setInvoiceId(null);
                    e.setSeller(authSeller);
                    e.setSoldBy(authUserMiniProfile);
                    e.setStockSold(persistStockSold(e));
                    e.setShop(shop);
                    if (!e.getMultiplePayment().isEmpty())
                        e.setMultiplePayment(persistMultiplePay(e));
                    if (e.getCustomerId() != null)
                        e.setCustomerId(customerMap.get(e.getCustomerId().getCustomerPhoneNumber()));
                    e.setPaymentModeVal(String.valueOf(e.getPaymentModeValue()));
                }).collect(Collectors.toList()));

        //Settled
        if (desktopPushObject.getShopStocks() != null && !desktopPushObject.getShopStocks().isEmpty())
            shopStocks = syncShopStock(shop, desktopPushObject.getShopStocks());

        status = (desktopPushObject.getCustomers() == null || customers.size() == desktopPushObject.getCustomers().size())
                && (desktopPushObject.getExpenses() == null || expenses.size() == desktopPushObject.getExpenses().size())
                && (desktopPushObject.getIncomes() == null || incomes.size() == desktopPushObject.getIncomes().size())
                && (desktopPushObject.getReturnedStocks() == null || returnedStocks.size() == desktopPushObject.getReturnedStocks().size())
                && (desktopPushObject.getInvoices() == null || invoices.size() == desktopPushObject.getInvoices().size())
                && (desktopPushObject.getShopStocks() == null || shopStocks.size() == desktopPushObject.getShopStocks().size());

        return new ResponseObject(status, status ? "Push Completed Successfully"
                : "Push could not complete, please try again.");
    }

    private void verifyShopCreationLimitViolation(BusinessOwner businessOwner) {

        if (shopRepository.findAllByCreatedBy(businessOwner.getBusinessOwnerEmail()).size()
                >= businessOwner.getPlan().getNumberOfShops())
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You have reached the maximum number of shops you can create in your plan/subscription", null);
    }

    private List<ShopStocks> syncShopStock(Shop shopInContext, List<ShopStocks> incomingStock){

        List<ShopStocks> shopStocksToSave = new ArrayList<>();

        List<ShopStocks> existingStock = shopStocksRepository.findAllByShop_ShopId(shopInContext.getShopId());
        List<String> existingNames = existingStock.stream().map(ShopStocks::getStockName).collect(Collectors.toList());

        if (existingStock.isEmpty()) return Collections.emptyList();

        incomingStock.forEach(sc -> {

            String incomingStockName = sc.getStockName();
            if (!existingNames.contains(incomingStockName)) return;
            if (existingNames.contains(incomingStockName)){

                ShopStocks existingShopStock = existingStock.stream()
                        .filter(es -> es.getStockName().equalsIgnoreCase(incomingStockName))
                        .limit(1).collect(toSingleton());

                sc.setShop(shopInContext);
                sc.setShopStockId(existingShopStock.getShopStockId());
                sc.setStockCategory(existingShopStock.getStockCategory());
                sc.setStockPurchasedFrom(existingShopStock.getStockPurchasedFrom());
                sc.setLastRestockPurchasedFrom(existingShopStock.getLastRestockPurchasedFrom());
                shopStocksToSave.add(sc);
            }
        });

        if (!shopStocksToSave.isEmpty()) return shopStocksRepository.saveAll(shopStocksToSave);

        return Collections.emptyList();
    }

    private void readyCustomerMap(List<String> sellers){
        customerMap = new HashMap<>();
        customerMap.putAll(sellers.stream().map(
            customerRepository::findAllByCreatedBy).flatMap(List::parallelStream)
                .collect(Collectors.toMap(Customer::getCustomerPhoneNumber, customer -> customer)));
    }

    private Set<StockSold> persistStockSold(Invoice invoice){

        return new HashSet<>(stockSoldRepository.saveAll(invoice.getStockSold().stream()
                .peek(ss -> ss.setStockSoldId(null)).collect(Collectors.toList())));
    }

    private List<MultiplePaymentMode> persistMultiplePay(Invoice invoice){

        return multiplePaymentModeRepository.saveAll(
                invoice.getMultiplePayment().stream().peek(ss -> {
                    ss.setMultiplePaymentModeId(null);
                    ss.setPaymentModeVal(String.valueOf(ss.getPaymentModeValue()));
                }).collect(Collectors.toList()));
    }

}
