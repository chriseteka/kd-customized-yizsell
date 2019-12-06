package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final BusinessOwnerRepository businessOwnerRepository;

    private final SellerRepository sellerRepository;

    @Autowired
    public AuthenticationServiceImpl(BusinessOwnerRepository businessOwnerRepository, SellerRepository sellerRepository)
    {
        this.businessOwnerRepository = businessOwnerRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        BusinessOwner businessOwner = businessOwnerRepository.findDistinctByBusinessOwnerEmail(email);

        if(businessOwner == null)
        {

            Seller seller = sellerRepository.findDistinctBySellerEmail(email);

            if(seller == null)
                throw new UsernameNotFoundException("Email not found, ensure you have provided a valid email address");
            else {

                //Collect the seller object
                return seller;
            }
        }
        else {

            //Collect the business owner object
            return businessOwner;
        }
    }
}
