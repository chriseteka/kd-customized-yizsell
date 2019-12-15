package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController;

import com.chrisworks.personal.inventorysystem.Backend.Configurations.JwtTokenProvider;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.RequestObject;
import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.ResponseObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final JwtTokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationController(JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
                                    AuthenticationService authenticationService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
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
}
