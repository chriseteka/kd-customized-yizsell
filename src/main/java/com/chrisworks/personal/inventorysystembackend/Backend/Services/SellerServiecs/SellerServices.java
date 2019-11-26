package com.chrisworks.personal.inventorysystembackend.Backend.Services.SellerServiecs;

import com.chrisworks.personal.inventorysystembackend.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystembackend.Backend.Services.GenericServices.GenericService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
@Transactional
public interface SellerServices extends GenericService<Seller> {

    //Services peculiar to sellers only
}
