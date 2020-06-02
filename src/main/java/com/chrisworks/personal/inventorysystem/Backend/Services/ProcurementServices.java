package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Procurement;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
public interface ProcurementServices extends CRUDServices<Procurement> {

    Procurement fetchProcurementByWaybillId(String waybillId);
}
