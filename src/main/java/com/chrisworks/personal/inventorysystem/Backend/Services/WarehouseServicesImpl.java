package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

            warehouseRepository.delete(warehouse);
            return warehouse;
        }).orElse(null);
    }

    @Override
    public Warehouse fetchWarehouseByWarehouseAttendant(String warehouseAttendantName) {

        return genericService.warehouseByWarehouseAttendantName(warehouseAttendantName);
    }

    @Override
    public Warehouse createWarehouse(Long businessOwnerId, Warehouse warehouse) {

        if (!AuthenticatedUserDetails.getAccount_type().equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            throw new InventoryAPIOperationException("Operation not allowed",
                    "Logged in user is not allowed to perform this operation", null);

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated id does not match id used for this request", null);

        if (warehouseRepository.findDistinctByWarehouseName(warehouse.getWarehouseName()) != null) throw new
                InventoryAPIOperationException("Warehouse name already exist",
                "A warehouse exist with the warehouse name: " + warehouse.getWarehouseName(), null);

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {

                    warehouse.setCreatedBy(AuthenticatedUserDetails.getUserFullName());
                    warehouse.setBusinessOwner(businessOwner);
                    return warehouseRepository.save(warehouse);
                }).orElse(null);
    }
}
