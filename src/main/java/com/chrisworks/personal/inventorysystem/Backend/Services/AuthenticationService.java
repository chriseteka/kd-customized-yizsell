package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.VerificationToken;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface AuthenticationService extends UserDetailsService {

    void createVerificationToken(BusinessOwner user, String token);

    Boolean resendVerificationToken(Long businessOwnerId);

    VerificationToken getVerificationToken(String token);

    BusinessOwner updateVerifiedBusinessOwner(BusinessOwner businessOwner);

    BusinessOwner validateAndVerifyBusinessOwnerEmail(String token);

    Boolean createPasswordResetToken(String email);

    Boolean resendPasswordResetToken(Long businessOwnerId);

    BusinessOwner getPasswordResetToken(String resetToken);

    BusinessOwner resetBusinessOwnerPassword(String resetToken, String newPassword);
}
