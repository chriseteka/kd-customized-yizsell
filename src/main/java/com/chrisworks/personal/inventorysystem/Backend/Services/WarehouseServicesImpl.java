package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.ShopRepository;
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

    private WarehouseRepository warehouseRepository;

    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    public WarehouseServicesImpl(WarehouseRepository warehouseRepository, BusinessOwnerRepository businessOwnerRepository) {
        this.warehouseRepository = warehouseRepository;
        this.businessOwnerRepository = businessOwnerRepository;
    }

    @Override
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouseUpdates) {

        AtomicReference<Warehouse> updatedWarehouse = new AtomicReference<>();

        warehouseRepository.findById(warehouseId).ifPresent(warehouse -> {

            if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

            warehouse.setUpdateDate(new Date());
            warehouse.setWarehouseAddress(warehouseUpdates.getWarehouseAddress());
            updatedWarehouse.set(warehouseRepository.save(warehouse));
        });

        return updatedWarehouse.get();
    }

    @Override
    public Warehouse warehouseById(Long warehouseId) {

        return warehouseRepository.findById(warehouseId)
                .map(warehouse -> {

                    if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                            InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

                    return warehouse;
                }).orElse(null);
    }

    @Override
    public List<Warehouse> fetchAllWarehouse() {

        return warehouseRepository.findAll()
                .stream()
                .filter(warehouse -> warehouse.getCreatedBy()
                        .equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public Warehouse deleteWarehouse(Long warehouseId) {

        return warehouseRepository.findById(warehouseId).map(warehouse -> {

            if (!warehouse.getCreatedBy().equalsIgnoreCase(AuthenticatedUserDetails.getUserFullName())) throw new
                    InventoryAPIOperationException("Not your warehouse", "Warehouse not created by you", null);

            warehouseRepository.delete(warehouse);
            return warehouse;
        }).orElse(null);
    }

    @Override
    public Warehouse createWarehouse(Long businessOwnerId, Warehouse warehouse) {

        if (null == businessOwnerId || businessOwnerId < 0 || !businessOwnerId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("business owner id error", "business owner id is empty or not a valid number", null);

        if (!businessOwnerId.equals(AuthenticatedUserDetails.getUserId())) throw new InventoryAPIOperationException
                ("business owner id error", "Authenticated id does not match id used for this request", null);

        if (warehouseRepository.findDistinctByWarehouseName(warehouse.getWarehouseName()) != null) throw new
                InventoryAPIOperationException("Warehouse name already exist",
                "A warehouse exist with the warehouse name: " + warehouse.getWarehouseName(), null);

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {
                    warehouse.setBusinessOwner(businessOwner);
                    return warehouseRepository.save(warehouse);
                }).orElse(null);
    }
}
