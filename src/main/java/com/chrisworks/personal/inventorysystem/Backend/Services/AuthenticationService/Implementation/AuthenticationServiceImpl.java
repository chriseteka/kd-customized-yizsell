package com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService.Implementation;

import com.chrisworks.personal.inventorysystem.Backend.Controllers.AuthController.Model.RequestObject;
import com.chrisworks.personal.inventorysystem.Backend.Services.AuthenticationService.AuthenticationService;
import org.springframework.stereotype.Service;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public boolean authenticateUser(RequestObject request) {

        //If after authentication, logged in user is a seller, update his row with lastLogin time and date
        //Set AuthUserDetails POJO with valid data
        return false;
    }

    @Override
    public boolean logUserOut() {

        //If logged in user is a seller, first update his last logout time field
        //Clear AuthUserDetails POJO
        return false;
    }
}
