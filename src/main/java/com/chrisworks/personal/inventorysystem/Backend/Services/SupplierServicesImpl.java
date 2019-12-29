package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SupplierRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SupplierServicesImpl implements SupplierServices {

    private final SupplierRepository supplierRepository;

    private final GenericService genericService;

    @Autowired
    public SupplierServicesImpl(SupplierRepository supplierRepository, GenericService genericService) {
        this.supplierRepository = supplierRepository;
        this.genericService = genericService;
    }

    @Override
    public Supplier createEntity(Supplier supplier) {

        return genericService.addSupplier(supplier);
    }

    @Override
    public Supplier updateEntity(Long supplierId, Supplier supplier) {

        return supplierRepository.findById(supplierId).map(supplierFound -> {

            if (!supplierFound.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                throw new InventoryAPIOperationException("Operation not allowed",
                        "You cannot update a supplier not created by you", null);

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

        return supplierRepository.findById(entityId).orElse(null);
    }

    @Override
    public List<Supplier> getEntityList() {

        return supplierRepository.findAll();
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
}
