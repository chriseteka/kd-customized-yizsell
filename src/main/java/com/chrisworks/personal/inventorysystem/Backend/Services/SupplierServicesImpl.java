package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SupplierRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SupplierServicesImpl implements SupplierServices {

    private final SupplierRepository supplierRepository;

    private final SellerRepository sellerRepository;

    private final GenericService genericService;

    @Autowired
    public SupplierServicesImpl(SupplierRepository supplierRepository, GenericService genericService,
                                SellerRepository sellerRepository) {
        this.supplierRepository = supplierRepository;
        this.sellerRepository = sellerRepository;
        this.genericService = genericService;
    }

    @Override
    public Supplier createEntity(Supplier supplier) {

        boolean match = getEntityList()
                .stream()
                .anyMatch(supplierFound -> supplierFound.getSupplierPhoneNumber()
                        .equalsIgnoreCase(supplier.getSupplierPhoneNumber()));

        if (match) throw new InventoryAPIOperationException("Supplier already exist",
                "Supplier already exist in your shop/business with the phone number: " + supplier.getSupplierPhoneNumber()
                        + ", hence it cannot add it to this business' list of suppliers.", null);

        return genericService.addSupplier(supplier);
    }

    @Override
    public Supplier updateEntity(Long supplierId, Supplier supplier) {

        return supplierRepository.findById(supplierId).map(supplierFound -> {

            if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER) &&
                    !supplierFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update a supplier not created by you", null);

            if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

                boolean match = genericService.sellersByAuthUserId()
                        .stream()
                        .map(Seller::getSellerEmail)
                        .anyMatch(sellerName -> sellerName.equalsIgnoreCase(supplierFound.getCreatedBy()));

                if (!match && !supplierFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                    throw new InventoryAPIOperationException("Operation not allowed",
                            "You cannot update a supplier not created by you or any of your sellers.", null);
            }

            supplierFound.setUpdateDate(new Date());
            supplierFound.setSupplierEmail(supplier.getSupplierEmail() != null ? supplier.getSupplierEmail()
                    : supplierFound.getSupplierEmail());
            supplierFound.setSupplierFullName(supplier.getSupplierFullName() != null ? supplier.getSupplierFullName()
                    : supplierFound.getSupplierFullName());

            return supplierRepository.save(supplierFound);
        }).orElse(null);
    }

    @Override
    public Supplier getSingleEntity(Long entityId) {

        return getEntityList()
                .stream()
                .filter(supplier -> supplier.getSupplierId().equals(entityId))
                .collect(toSingleton());
    }

    @Override
    public List<Supplier> getEntityList() {

        Set<Supplier> supplierSet = new HashSet<>(Collections.emptySet());

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.SHOP_SELLER)
                || AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

            Seller seller = sellerRepository.findDistinctBySellerEmail(AuthenticatedUserDetails.getUserFullName());
            List<Seller> sellerList = sellerRepository.findAllByCreatedBy(seller.getCreatedBy());

            supplierSet.addAll(sellerList
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(supplierRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));
            supplierSet.addAll(supplierRepository.findAllByCreatedBy(seller.getCreatedBy()));
        }

        if (AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER)){

            supplierSet.addAll(genericService.sellersByAuthUserId()
                    .stream()
                    .map(Seller::getSellerEmail)
                    .map(supplierRepository::findAllByCreatedBy)
                    .flatMap(List::parallelStream)
                    .collect(Collectors.toList()));

            supplierSet.addAll(supplierRepository
                    .findAllByCreatedBy(AuthenticatedUserDetails.getUserFullName()));
        }

        return new ArrayList<>(supplierSet);
    }

    @Override
    public Supplier deleteEntity(Long supplierId) {

        if (AuthenticatedUserDetails.getAccount_type() == null ||
                !AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return supplierRepository.findById(supplierId).map(supplier -> {

            boolean match = getEntityList()
                    .stream()
                    .anyMatch(e -> e.getCreatedBy().equalsIgnoreCase(supplier.getCreatedBy()));

            if (match) {
                supplierRepository.delete(supplier);
                return supplier;
            }
            else throw new InventoryAPIOperationException("Operation not allowed",
                    "You cannot delete a supplier not created by you or any of your sellers", null);
        }).orElse(null);
    }

    @Override
    public Supplier fetchSupplierByPhoneNumber(String phoneNumber) {

        return getEntityList()
                .stream()
                .filter(supplier -> supplier.getSupplierPhoneNumber().equalsIgnoreCase(phoneNumber))
                .collect(toSingleton());
    }

    @Override
    public Supplier fetchSupplierByName(String supplierName) {

        return getEntityList()
                .stream()
                .filter(supplier -> supplier.getSupplierFullName().equalsIgnoreCase(supplierName))
                .collect(toSingleton());
    }

    @Override
    public List<Supplier> fetchSupplierByCreator(String createdBy) {

        return getEntityList()
                .stream()
                .filter(supplier -> supplier.getCreatedBy().equalsIgnoreCase(createdBy))
                .collect(Collectors.toList());
    }

}
