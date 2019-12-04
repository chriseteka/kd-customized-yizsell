package com.chrisworks.personal.inventorysystem.Backend.Services.StockSoldServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.StockSold;
import com.chrisworks.personal.inventorysystem.Backend.Services.StockSoldServices.StockSoldServices;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class StockSoldServicesImpl implements StockSoldServices {

    @Override
    public StockSold createEntity(StockSold stockSold) {
        return null;
    }

    @Override
    public StockSold updateEntity(Long entityId, StockSold stockSold) {
        return null;
    }

    @Override
    public StockSold getSingleEntity(Long entityId) {
        return null;
    }

    @Override
    public List<StockSold> getEntityList() {
        return null;
    }

    @Override
    public StockSold deleteEntity(Long entityId) {
        return null;
    }
}
