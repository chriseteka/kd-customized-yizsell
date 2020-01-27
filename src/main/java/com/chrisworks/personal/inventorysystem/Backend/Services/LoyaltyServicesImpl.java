package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Loyalty;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.LoyaltyRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 1/16/2020
 * @email chriseteka@gmail.com
 */
@Service
public class LoyaltyServicesImpl implements LoyaltyServices {

    private final CustomerRepository customerRepository;

    private final LoyaltyRepository loyaltyRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public LoyaltyServicesImpl(CustomerRepository customerRepository, LoyaltyRepository loyaltyRepository,
                               SellerRepository sellerRepository) {
        this.customerRepository = customerRepository;
        this.loyaltyRepository = loyaltyRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Loyalty addCustomerToLoyaltyPlan(Long loyaltyId, Long customerId) {

        preAuthorize();

        return loyaltyRepository.findById(loyaltyId).map(loyalty -> {

            if (!loyalty.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Loyalty plan not yours",
                        "Loyalty plan with id: " + " was not found in your list of loyalty plans", null);

            return customerRepository.findById(customerId).map(customer -> {

                Set<Customer> customerSet = loyalty.getCustomers();

                boolean match = customerSet
                        .stream()
                        .anyMatch(c -> c.getCustomerPhoneNumber().equalsIgnoreCase(customer.getCustomerPhoneNumber()));

                if (match) throw new InventoryAPIOperationException("Customer has bee subscribed",
                        "Customer with id: " + customerId + " may have been subscribed to a loyalty plan, " +
                                "remove this customer from their pre-existing plan, and try again.", null);

                customer.setIsLoyal(true);
                customerSet.add(customerRepository.save(customer));
                loyalty.setCustomers(customerSet);

                return loyaltyRepository.save(loyalty);
            }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Customer not found",
                    "Customer with id: " + customerId + " was not found.", null));
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Loyalty plan not found",
                "Loyalty plan with id: " + loyaltyId + " was not found.", null));
    }

    @Override
    public Loyalty findLoyaltyPlanByCustomerId(Long customerId) {

        preAuthorize();

        return customerRepository.findById(customerId).map(customer -> {

            Loyalty loyalty = loyaltyRepository.findDistinctByCustomers(customer);

            if (loyalty == null) throw new InventoryAPIOperationException("Customer has no loyalty",
                    "Customer may have not been subscribed to any loyalty plan", null);

            return loyalty;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Customer not found",
                "Customer with id: " + customerId + " was not found.", null));
    }

    @Override
    public Loyalty removeCustomerFromPlan(Long loyaltyId, Long customerId) {

        Loyalty loyaltyPlan = findLoyaltyPlanByCustomerId(customerId);

        if (!loyaltyPlan.getLoyaltyId().equals(loyaltyId))
            throw new InventoryAPIOperationException("Customer not assigned",
                    "Customer may have not been assigned to the specified loyalty id", null);

        Set<Customer> customerSet = loyaltyPlan.getCustomers();
        Customer customerToRemove = customerSet
                .stream()
                .filter(customer -> customer.getCustomerId().equals(customerId))
                .collect(toSingleton());

        customerSet.remove(customerToRemove);
        customerToRemove.setIsLoyal(false);
        customerToRemove.setNumberOfPurchasesAfterLastReward(0);
        customerToRemove.setRecentPurchasesAmount(BigDecimal.ZERO);
        customerRepository.save(customerToRemove);

        loyaltyPlan.setCustomers(customerSet);
        return loyaltyRepository.save(loyaltyPlan);
    }

    @Override
    public Loyalty createEntity(Loyalty loyalty) {

        boolean match = getEntityList()
                .stream()
                .map(Loyalty::getLoyaltyName)
                .anyMatch(loyaltyName -> loyaltyName.equalsIgnoreCase(loyalty.getLoyaltyName()));

        if (match) throw new InventoryAPIDuplicateEntryException("Duplicate loyalty name",
                "Loyalty name already exist, change this field and try again", null);

        return loyaltyRepository.save(loyalty);
    }

    @Override
    public Loyalty updateEntity(Long entityId, Loyalty loyalty) {

        Loyalty loyaltyFound = getSingleEntity(entityId);
        loyaltyFound.setThreshold(loyalty.getThreshold());
        loyaltyFound.setLoyaltyName(loyalty.getLoyaltyName());
        loyalty.setNumberOfDaysBeforeReward(loyalty.getNumberOfDaysBeforeReward());

        return loyaltyRepository.save(loyaltyFound);
    }

    @Override
    public Loyalty getSingleEntity(Long entityId) {

        preAuthorize();

        return loyaltyRepository.findById(entityId).map(loyalty -> {

            if (!loyalty.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Loyalty plan not yours",
                        "Loyalty plan with id: " + " was not found in your list of loyalty plans", null);

            return loyalty;

        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Loyalty plan not found",
                "Loyalty plan with id: " + entityId + " was not found.", null));
    }

    @Override
    public List<Loyalty> getEntityList() {

        if (AuthenticatedUserDetails.getAccount_type() == null) throw new InventoryAPIOperationException("Unknown user",
                "Cannot determine the account type of the logged in user", null);

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            return loyaltyRepository.findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName());
        else {

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());

            return loyaltyRepository.findAllByCreatedBy(seller.getCreatedBy());
        }
    }

    @Override
    public Loyalty deleteEntity(Long entityId) {

        Loyalty loyalty = getSingleEntity(entityId);

        loyaltyRepository.delete(loyalty);

        return loyalty;
    }

    private void preAuthorize(){

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);
    }
}
