package com.chrisworks.personal.inventorysystem.Backend.Services.CRUDServices;

import java.util.List;

/**
 * @author Chris_Eteka
 * @since 11/28/2019
 * @email chriseteka@gmail.com
 */
public interface CRUDServices<T extends Object> {

    T createEntity(T t);

    T updateEntity(Long entityId, T t);

    T getSingleEntity(Long entityId);

    List<T> getEntityList();

    T deleteEntity(Long entityId);
}
