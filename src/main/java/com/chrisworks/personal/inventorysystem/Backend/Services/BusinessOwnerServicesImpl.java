package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDataValidationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.*;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.OnRegistrationCompleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class BusinessOwnerServicesImpl implements BusinessOwnerServices {

    private BusinessOwnerRepository businessOwnerRepository;

    private ApplicationEventPublisher eventPublisher;

    private SellerRepository sellerRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public BusinessOwnerServicesImpl(BusinessOwnerRepository businessOwnerRepository, ApplicationEventPublisher eventPublisher,
                                     BCryptPasswordEncoder passwordEncoder, SellerRepository sellerRepository) {

        this.businessOwnerRepository = businessOwnerRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
        this.sellerRepository = sellerRepository;
    }

    @Transactional
    @Override
    public BusinessOwner createAccount(BusinessOwner businessOwner, WebRequest request) {

        if (businessOwnerRepository.findDistinctByBusinessOwnerEmail(businessOwner.getBusinessOwnerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A business account already exist with the email address: " +
                businessOwner.getBusinessOwnerEmail(), null);

        if (sellerRepository.findDistinctBySellerEmail(businessOwner.getBusinessOwnerEmail()) != null) throw new
                InventoryAPIDuplicateEntryException("Email already exist", "A seller account already exist with the email address: " +
                businessOwner.getBusinessOwnerEmail(), null);

        businessOwner.setBusinessOwnerPassword
                (passwordEncoder.encode(businessOwner.getBusinessOwnerPassword()));

        BusinessOwner businessOwnerCreated = businessOwnerRepository.save(businessOwner);

        try {
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(businessOwnerCreated));
        } catch (Exception e) {

            e.printStackTrace();
            throw new InventoryAPIOperationException(e.getLocalizedMessage(), e.getMessage(), null);
        }

        return businessOwnerCreated;
    }

    @Override
    public Seller activateSeller(Long sellerId) {

        return sellerRepository.findById(sellerId).map(seller -> {

            seller.setIsActive(true);
            return sellerRepository.save(seller);
        }).orElse(null);
    }

    @Override
    public Seller deactivateSeller(Long sellerId) {

        return sellerRepository.findById(sellerId).map(seller -> {

            seller.setIsActive(false);
            return sellerRepository.save(seller);
        }).orElse(null);
    }

    @Override
    public Seller updateSeller(Long sellerId, Seller sellerUpdates) {

        return sellerRepository.findById(sellerId).map(seller -> {

            seller.setSellerPassword(sellerUpdates.getSellerPassword() != null ?
                    passwordEncoder.encode(sellerUpdates.getPassword()) : seller.getSellerPassword());
            seller.setSellerFullName(sellerUpdates.getSellerFullName());
            seller.setSellerPhoneNumber(sellerUpdates.getSellerPhoneNumber());
            seller.setSellerAddress(sellerUpdates.getSellerAddress());
            seller.setSellerEmail(sellerUpdates.getSellerEmail());
            seller.setUpdateDate(new Date());
            return sellerRepository.save(seller);
        }).orElse(null);
    }

    @Override
    public BusinessOwner updateAccount(Long businessOwnerId, BusinessOwner businessOwnerUpdates) {

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated user id does not match id from request", null);

        if (null == businessOwnerUpdates) throw new InventoryAPIOperationException
                ("could not find an entity to save", "Could not find business owner entity to save", null);

        AtomicReference<BusinessOwner> updatedDetails = new AtomicReference<>();

        Optional<BusinessOwner> optionalBusinessOwner = businessOwnerRepository.findById(businessOwnerId);

        if (!optionalBusinessOwner.isPresent()) throw new InventoryAPIResourceNotFoundException
                ("Entity to update not found", "No business owner exist with id: " + businessOwnerId, null);

        optionalBusinessOwner.ifPresent(businessOwner -> {

            businessOwner.setBusinessOwnerFullName(businessOwnerUpdates.getBusinessOwnerFullName());
            businessOwner.setBusinessOwnerPhoneNumber(businessOwnerUpdates.getBusinessOwnerPhoneNumber());
            businessOwner.setBusinessName(businessOwnerUpdates.getBusinessName());
            businessOwner.setUpdateDate(new Date());
            updatedDetails.set(businessOwnerRepository.save(businessOwner));
        });

        return updatedDetails.get();
    }
}
