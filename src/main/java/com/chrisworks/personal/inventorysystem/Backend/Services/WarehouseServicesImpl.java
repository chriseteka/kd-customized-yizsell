package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class WarehouseServicesImpl implements WarehouseServices {

    private final WarehouseRepository warehouseRepository;

    private final BusinessOwnerRepository businessOwnerRepository;

    private final GenericService genericService;

    @Autowired
    public WarehouseServicesImpl(WarehouseRepository warehouseRepository, BusinessOwnerRepository businessOwnerRepository,
                                 GenericService genericService) {
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerRepository = businessOwnerRepository;
        this.genericService = genericService;
    }

    @Override
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

            warehouse.setUpdateDate(new Date());
            warehouse.setWarehouseName(warehouseUpdates.getWarehouseName());
            warehouse.setWarehouseAddress(warehouseUpdates.getWarehouseAddress());
            return warehouseRepository.save(warehouse);
        }).orElse(null);
    }

    @Override
    public Warehouse warehouseById(Long warehouseId) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findById(warehouseId)
                .map(warehouse -> {

                    if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                            InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

                    return warehouse;
                }).orElse(null);
    }

    @Override
    public List<Warehouse> fetchAllWarehouse() {

        return genericService.warehouseByAuthUserId();
    }

    @Override
    public Warehouse deleteWarehouse(Long warehouseId) {

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

            warehouseRepository.delete(warehouse);
            return warehouse;
        }).orElseThrow(() -> new InventoryAPIResourceNotFoundException("Warehouse not found",
                "Warehouse with id: " + warehouseId + " was not found", null));
    }

    @Override
    public Warehouse fetchWarehouseByWarehouseAttendant(String warehouseAttendantName) {

        return genericService.warehouseByWarehouseAttendantName(warehouseAttendantName);
    }

    @Override
    public List<Warehouse> deleteWarehouses(Long... warehouseIds) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        List<Long> warehouseIdsToDelete = Arrays.asList(warehouseIds);
        if (warehouseIdsToDelete.size() == 1)
            return Collections.singletonList(deleteWarehouse(warehouseIdsToDelete.get(0)));

        List<Warehouse> warehouseListToDelete = fetchAllWarehouse().stream()
                .filter(warehouse -> warehouseIdsToDelete.contains(warehouse.getWarehouseId()))
                .collect(Collectors.toList());

        if (!warehouseListToDelete.isEmpty()) warehouseRepository.deleteAll(warehouseListToDelete);

        return warehouseListToDelete;
    }

    @Override
    public Warehouse createWarehouse(Long businessOwnerId, Warehouse warehouse) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (!AuthenticatedUserDetails.getHasWarehouse()) throw new InventoryAPIOperationException("Operation not allowed",
                "Logged in user has no right to create warehouse as this is not included in his plan", null);

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated id does not match id used for this request", null);

        if (warehouseRepository.findDistinctByWarehouseNameAndCreatedBy(warehouse.getWarehouseName(),
                AuthenticatedUserDetails.getUserFullName()) != null) throw new
                InventoryAPIOperationException("Warehouse name already exist",
                "A warehouse exist with the warehouse name: " + warehouse.getWarehouseName(), null);

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {

                    verifyWarehouseCreationLimitViolation(businessOwner);
                    warehouse.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    warehouse.setBusinessOwner(businessOwner);
                    return warehouseRepository.save(warehouse);
                }).orElse(null);
    }

    private void verifyWarehouseCreationLimitViolation(BusinessOwner businessOwner) {

        if (warehouseRepository.findAllByCreatedBy(businessOwner.getBusinessOwnerEmail()).size()
                >= businessOwner.getPlan().getNumberOfWarehouses())
            throw new InventoryAPIOperationException("Operation not allowed",
                    "You have reached the maximum number of warehouses you can create in your plan/subscription", null);
    }
}
