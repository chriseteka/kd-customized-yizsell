package com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.RequestObject;
import com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Chris_Eteka
 * @since 11/27/2019
 * @email chriseteka@gmail.com
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/signIn", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid RequestObject request){

        boolean authenticated = authenticationService.authenticateUser(request);

        return authenticated ? ResponseEntity.ok(true) : ResponseEntity.ok(false);
    }
}
