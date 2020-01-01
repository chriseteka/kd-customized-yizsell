package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.PasswordResetToken;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.VerificationToken;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIDuplicateEntryException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.PasswordResetRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


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

    private final PasswordResetRepository passwordResetRepository;

    private final MailServices mailServices;

    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${email.sender}") private String emailSender;

    @Autowired
    public AuthenticationServiceImpl(BusinessOwnerRepository businessOwnerRepository, SellerRepository sellerRepository,
                                     VerificationTokenRepository tokenRepository, PasswordResetRepository passwordResetRepository,
                                     MailServices mailServices, BCryptPasswordEncoder passwordEncoder)
    {
        this.businessOwnerRepository = businessOwnerRepository;
        this.sellerRepository = sellerRepository;
        this.tokenRepository = tokenRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.mailServices = mailServices;
        this.passwordEncoder = passwordEncoder;
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

        VerificationToken verificationToken = tokenRepository.findDistinctByBusinessOwner(user);

        if (verificationToken != null) tokenRepository.delete(verificationToken);

        VerificationToken generatedToken = new VerificationToken(user, token);
        tokenRepository.save(generatedToken);
    }

    @Override
    public Boolean resendVerificationToken(Long businessOwnerId) {

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {

                    VerificationToken verificationToken = tokenRepository.findDistinctByBusinessOwner(businessOwner);

                    if (null == verificationToken)
                        throw new InventoryAPIOperationException("Could not get verification token",
                                    "Could not get a verification token for the business owner id passed, try again", null);

                    String recipientAddress = businessOwner.getBusinessOwnerEmail();
                    String subject = "Registration Confirmation";
                    String message = "Confirm your registration by copying the following token and pasting where required: ";
                    String body = message + verificationToken.getToken();

                    EmailObject emailObject = new EmailObject(emailSender, recipientAddress, subject, body, Collections.emptyList());

                    mailServices.sendAutomatedEmail(emailObject);

                    return true;
                }).orElse(null);
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
                ("No verification token found", "No verification token found for verification id: " + token, null);

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0)throw new
                InventoryAPIOperationException("Verification token expired", "Verification token expired", null);

        BusinessOwner businessOwner = verificationToken.getBusinessOwner();

        if(businessOwner.getVerified())throw new InventoryAPIDuplicateEntryException("Account already verified",
                "Your account is already verified, please log in to view your dashboard", null);

//        businessOwner.isEnabled()
        businessOwner.setIsActive(true);
        businessOwner.setVerified(true);
        return this.updateVerifiedBusinessOwner(businessOwner);
    }

    @Override
    @Transactional
    public Boolean createPasswordResetToken(String email) {

        BusinessOwner businessOwner = businessOwnerRepository.findDistinctByBusinessOwnerEmail(email);

        if (null == businessOwner) throw new InventoryAPIResourceNotFoundException
                ("Business Owner not found", "No business owner with email: " + email + " was found in the system", null);

        PasswordResetToken token = passwordResetRepository.findDistinctByBusinessOwner(businessOwner);

        if (null != token) passwordResetRepository.delete(token);

        String passwordResetToken = String.valueOf(System.currentTimeMillis()).substring(6, 12);

        PasswordResetToken resetToken = passwordResetRepository.save(new PasswordResetToken(businessOwner, passwordResetToken));

        if (null == resetToken) throw new InventoryAPIOperationException
                ("Could not generate a reset token", "Could not generate a reset token at the moment, try later", null);

        String recipientAddress = businessOwner.getBusinessOwnerEmail();
        String subject = "Password Reset Notification";
        String message = "Reset your password by copying the following token and pasting where required: ";
        String body = message + passwordResetToken;

        EmailObject emailObject = new EmailObject(emailSender, recipientAddress, subject, body, Collections.emptyList());

        mailServices.sendAutomatedEmail(emailObject);

        return true;
    }

    @Override
    public Boolean resendPasswordResetToken(Long businessOwnerId) {

        return businessOwnerRepository.findById(businessOwnerId)
                .map(businessOwner -> {

                    PasswordResetToken resetToken = passwordResetRepository.findDistinctByBusinessOwner(businessOwner);

                    if (null == resetToken)
                        throw new InventoryAPIOperationException("Could not get password reset token",
                                "Could not get a password reset token for the business owner id passed, try again", null);

                    String recipientAddress = businessOwner.getBusinessOwnerEmail();
                    String subject = "Password Reset Notification";
                    String message = "Reset your password by copying the following token and pasting where required: ";
                    String body = message + resetToken.getToken();

                    EmailObject emailObject = new EmailObject(emailSender, recipientAddress, subject, body, Collections.emptyList());

                    mailServices.sendAutomatedEmail(emailObject);

                    return true;
                }).orElse(null);
    }

    @Override
    public BusinessOwner getPasswordResetToken(String resetToken) {

        PasswordResetToken resetTokenFound = passwordResetRepository.findDistinctByToken(resetToken);

        if (resetTokenFound == null) throw new InventoryAPIOperationException
                ("No password reset token found", "No password reset token found with token id: " + resetToken, null);

        Calendar cal = Calendar.getInstance();
        if ((resetTokenFound.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0)throw new
                InventoryAPIOperationException("Password reset token expired", "Password reset token expired", null);

        return resetTokenFound.getBusinessOwner();
    }

    @Override
    public BusinessOwner resetBusinessOwnerPassword(String resetToken, String newPassword) {

        PasswordResetToken token = passwordResetRepository.findDistinctByToken(resetToken);

        if (null == token) throw new InventoryAPIResourceNotFoundException("Password reset token not found",
                "Could not find password reset token with token id: " + resetToken, null);

        BusinessOwner businessOwner = token.getBusinessOwner();

        if (null == businessOwner) throw new InventoryAPIResourceNotFoundException
                ("Business Owner not found", "Business Owner not found", null);

        businessOwner.setUpdateDate(new Date());
        businessOwner.setBusinessOwnerPassword(passwordEncoder.encode(newPassword));

        return businessOwnerRepository.save(businessOwner);
    }
}
