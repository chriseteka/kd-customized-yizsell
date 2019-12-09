package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Stock;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Warehouse;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.StockRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.WarehouseRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.toSingleton;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockServicesImpl implements StockServices {

    private StockRepository stockRepository;

    private WarehouseRepository warehouseRepository;

    private GenericService genericService;

    @Autowired
    public StockServicesImpl(StockRepository stockRepository, WarehouseRepository warehouseRepository,
                             GenericService genericService) {
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
        this.genericService = genericService;
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
    public List<Stock> unApprovedStock(Long warehouseId) {

        if (null == warehouseId || warehouseId < 0 || !warehouseId.toString().matches("\\d+")) throw new
                InventoryAPIOperationException("warehouse id error", "warehouse id is empty or not a valid number", null);

        return warehouseRepository.findById(warehouseId)
                .map(stockRepository::findAllByWarehousesAndApprovedIsFalse)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Stock> unApprovedStockByCreator(String createdBy) {

        return stockRepository.findAllByCreatedByAndAndApprovedIsFalse(createdBy);
    }

    @Override
    public Stock approveStock(Long stockId) {

        if (ACCOUNT_TYPE.SELLER.equals(AuthenticatedUserDetails.getAccount_type())) throw new InventoryAPIOperationException
                ("Operation not allowed", "Logged in user cannot perform this operation", null);

        Stock stockFound = genericService.allWarehouseByAuthUserId()
                .stream()
                .map(Warehouse::getWarehouseId)
                .map(this::unApprovedStock)
                .flatMap(List::parallelStream)
                .filter(stock -> stock.getStockId().equals(stockId))
                .collect(toSingleton());

        stockFound.setUpdateDate(new Date());
        stockFound.setApproved(true);
        stockFound.setApprovedDate(new Date());
        stockFound.setApprovedBy(AuthenticatedUserDetails.getUserFullName());

        return stockRepository.save(stockFound);
    }

    @Override
    public List<Stock> approveStockList(List<Long> stockIdList) {

        return stockIdList.parallelStream()
                .map(this::approveStock)
                .collect(Collectors.toList());
    }
}
