package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockServicesImpl implements StockServices {

    private StockRepository stockRepository;

    private WarehouseRepository warehouseRepository;

    @Autowired
    public StockServicesImpl(StockRepository stockRepository, WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public Stock createEntity(Stock stock) {
        return null;
    }

    @Override
    public Stock updateEntity(Long entityId, Stock stock) {
        return null;
    }

    @Override
    public Stock getSingleEntity(Long entityId) {
        return null;
    }

    @Override
    public List<Stock> getEntityList() {
        return null;
    }

    @Override
    public Stock deleteEntity(Long entityId) {
        return null;
    }

    @Override
    public List<Stock> allStockByWarehouseId(Long warehouseId) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        return warehouseRepository.findById(warehouseId)
                .map(stockRepository::findAllByWarehouses)
                .orElse(Collections.emptyList());
    }

    @Override
    public Boolean approveStock(Long stockId) {

        AtomicReference<Boolean> approveStatus = new AtomicReference<>(false);

        stockRepository.findById(stockId).ifPresent(unApprovedStock -> {

            unApprovedStock.setApproved(true);
            unApprovedStock.setApprovedDate(new Date());
            unApprovedStock.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

            if (stockRepository.save(unApprovedStock) != null) approveStatus.set(true);
        });

        return approveStatus.get();
    }

    @Override
    public Boolean approveStockList(List<Long> stockIdList) {

        AtomicReference<Boolean> stockApprovedFlag = new AtomicReference<>(false);

        stockIdList.parallelStream().forEach(stockId -> stockApprovedFlag.set(this.approveStock(stockId)));

        return stockApprovedFlag.get();
    }
}
