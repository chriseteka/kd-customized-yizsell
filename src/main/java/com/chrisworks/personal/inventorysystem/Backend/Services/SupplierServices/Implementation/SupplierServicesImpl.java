package com.chrisworks.personal.inventorysystem.Backend.Services.SupplierServices.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Supplier;
import com.chrisworks.personal.inventorysystem.Backend.Services.SupplierServices.SupplierServices;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 12/2/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SupplierServicesImpl implements SupplierServices {
    @Override
    public Supplier createEntity(Supplier supplier) {
        return null;
    }

    @Override
    public Supplier updateEntity(Long entityId, Supplier supplier) {
        return null;
    }

    @Override
    public Supplier getSingleEntity(Long entityId) {
        return null;
    }

    @Override
    public List<Supplier> getEntityList() {
        return null;
    }

    @Override
    public Supplier deleteEntity(Long entityId) {
        return null;
    }
}
