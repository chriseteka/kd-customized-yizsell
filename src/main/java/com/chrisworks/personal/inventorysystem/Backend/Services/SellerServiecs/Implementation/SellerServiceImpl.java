package com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.SellerServiecs.SellerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class SellerServiceImpl implements SellerServices {

    private SellerRepository sellerRepository;

    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Seller updateAccount(Long userId, Seller sellerUpdates) {

        AtomicReference<Seller> updatedSeller = new AtomicReference<>();

        sellerRepository.findById(userId).ifPresent(seller -> {

            seller.setSellerAddress(sellerUpdates.getSellerAddress() != null ?
                    sellerUpdates.getSellerAddress() : seller.getSellerAddress());
            seller.setSellerFullName(sellerUpdates.getSellerFullName() != null ?
                    sellerUpdates.getSellerFullName() : seller.getSellerFullName());
            seller.setSellerPhoneNumber(sellerUpdates.getSellerPhoneNumber() != null ?
                    sellerUpdates.getSellerPhoneNumber() : seller.getSellerPhoneNumber());
            updatedSeller.set(sellerRepository.save(seller));
        });

        return updatedSeller.get();
    }
}
