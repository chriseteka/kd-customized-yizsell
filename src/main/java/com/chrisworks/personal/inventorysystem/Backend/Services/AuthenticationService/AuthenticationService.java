package com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.RequestObject;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
public interface AuthenticationService {

    boolean authenticateUser(RequestObject request);

    boolean logUserOut();
}
