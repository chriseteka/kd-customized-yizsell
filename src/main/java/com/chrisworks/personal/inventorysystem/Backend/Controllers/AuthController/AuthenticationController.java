package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController;

import com.chrisworks.personal.inventorysystem.Backend.Configurations.JwtTokenProvider;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.PasswordResetObject;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.RequestObject;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService;
import com.chrisworks.personal.inventorysystem.Backend.Services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.SecurityConstants.TOKEN_PREFIX;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final JwtTokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationController(JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
                                    AuthenticationService authenticationService, RefreshTokenService refreshTokenService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(path = "/signIn", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid RequestObject request){

        String token;

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {

            BusinessOwner userDetails = (BusinessOwner) authentication.getPrincipal();
            token = TOKEN_PREFIX + tokenProvider.generateBusinessOwnerToken(userDetails);

        }catch (Exception e){

            Seller userDetails = (Seller) authentication.getPrincipal();
            token = TOKEN_PREFIX + tokenProvider.generateSellerToken(userDetails);
        }


        return ResponseEntity.ok(new ResponseObject(true, token));
    }

    @PutMapping(path = "/registrationConfirm")
    public ResponseEntity<?> confirmBusinessOwnerRegistrationEmail(@RequestParam String token) {

        BusinessOwner verifiedBusinessOwner = authenticationService.validateAndVerifyBusinessOwnerEmail(token);

        if (null == verifiedBusinessOwner) throw new InventoryAPIOperationException
                ("Business Owner not verified", "Business Owner not verified", null);

        return ResponseEntity.ok(verifiedBusinessOwner);
    }

    @PutMapping(path = "/reset/password")
    public ResponseEntity<?> resetBusinessOwnerPasswordRequest(@RequestParam String email){

        Boolean passwordResetInitiated = authenticationService.createPasswordResetToken(email);

        return ResponseEntity.ok(passwordResetInitiated);
    }

    @GetMapping(path = "/reset/password")
    public ResponseEntity<?> verifyResetPasswordToken(@RequestParam String resetToken){

        BusinessOwner businessOwnerByPasswordResetToken = authenticationService.getPasswordResetToken(resetToken);

        if (businessOwnerByPasswordResetToken == null) throw new InventoryAPIOperationException
                ("Business Owner not found", "Business Owner not found, try again later", null);

        return ResponseEntity.ok(businessOwnerByPasswordResetToken);
    }

    @PutMapping(path = "/complete/password/reset")
    public ResponseEntity<?> completePasswordReset(@RequestParam String resetToken,
                                                   @RequestBody @Valid PasswordResetObject passwordResetObject){

        BusinessOwner businessOwnerPassReset = authenticationService
                .resetBusinessOwnerPassword(resetToken, passwordResetObject.getNewPassword());

        if (null == businessOwnerPassReset) throw new InventoryAPIOperationException
                ("Password reset could not complete", "Password reset did not complete, try again later", null);

        return ResponseEntity.ok(businessOwnerPassReset);
    }

    @GetMapping(path = "/resend/verificationToken/byId")
    public ResponseEntity<?> resendBusinessOwnerVerificationToken(@RequestParam Long businessOwnerId){

        Boolean isSent = authenticationService.resendVerificationToken(businessOwnerId);

        if (!isSent) throw new InventoryAPIOperationException("Verification token not sent",
                "Could not resend verification token, review your inputs and try again", null);

        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/resend/passResetToken/byId")
    public ResponseEntity<?> resendBusinessOwnerPasswordResetToken(@RequestParam Long businessOwnerId){

        Boolean isSent = authenticationService.resendPasswordResetToken(businessOwnerId);

        if (!isSent) throw new InventoryAPIOperationException("Password reset token not sent",
                "Could not resend password reset token, review your inputs and try again", null);

        return ResponseEntity.ok(true);
    }

    @GetMapping(path = "/refresh")
    public ResponseEntity<?> refreshAuthToken(@RequestParam String token, @RequestParam int accountType){

        ACCOUNT_TYPE account_type = ACCOUNT_TYPE.of(accountType);

        if (account_type == null) throw new InventoryAPIOperationException("Invalid account type",
                "Invalid account type passed, account type can ony be 100, 200 or 300", null);

        Object object = refreshTokenService.refreshLoggedInUserToken(token, account_type);

        if (object == null) return ResponseEntity.badRequest().build();

        ResponseObject responseObject;

        if (account_type.equals(ACCOUNT_TYPE.BUSINESS_OWNER))
            responseObject = new ResponseObject(true,
                    TOKEN_PREFIX + tokenProvider.generateBusinessOwnerToken((BusinessOwner) object));
        else responseObject = new ResponseObject(true,
                TOKEN_PREFIX + tokenProvider.generateSellerToken((Seller) object));

        return ResponseEntity.ok(responseObject);
    }
}

//class DefaultPreAuthenticationChecks implements UserDetailsChecker {
//
//    @Override
//    public void check(UserDetails user) {
//        if (!user.isEnabled()) {
//
//            throw new InventoryAPIOperationException("Account is not verified",
//                    "Your account is not verified yet, check your email for verification code", null);
//        }
//    }
//}
