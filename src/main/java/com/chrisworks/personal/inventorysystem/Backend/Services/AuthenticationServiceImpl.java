package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.VerificationToken;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Calendar;


/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final BusinessOwnerRepository businessOwnerRepository;

    private final SellerRepository sellerRepository;

    private final VerificationTokenRepository tokenRepository;

    @Autowired
    public AuthenticationServiceImpl(BusinessOwnerRepository businessOwnerRepository, SellerRepository sellerRepository,
                                     VerificationTokenRepository tokenRepository)
    {
        this.businessOwnerRepository = businessOwnerRepository;
        this.sellerRepository = sellerRepository;
        this.tokenRepository = tokenRepository;
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

            return businessOwner;
        }
    }

    @Override
    public void createVerificationToken(BusinessOwner user, String token) {

        VerificationToken generatedToken = new VerificationToken(user, token);
        tokenRepository.save(generatedToken);
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Override
    public BusinessOwner updateVerifiedBusinessOwner(BusinessOwner businessOwner) {
        return businessOwnerRepository.save(businessOwner);
    }

    @Override
    public BusinessOwner validateAndVerifyBusinessOwnerEmail(String token) {

        VerificationToken verificationToken = this.getVerificationToken(token);

        if (verificationToken == null) throw new InventoryAPIOperationException
                ("No verification token found", "No verification token found", null);

        BusinessOwner businessOwner = verificationToken.getBusinessOwner();

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0)throw new
                InventoryAPIOperationException("Verification token expired", "Verification token expired", null);

        businessOwner.setIsActive(true);
        businessOwner.setVerified(true);
        return this.updateVerifiedBusinessOwner(businessOwner);
    }
}
