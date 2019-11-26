package com.chrisworks.personal.inventorysystembackend.Backend.Services.AuthenticationService;

import org.springframework.stereotype.Service;

/**
 * @author Chris_Eteka
 * @since 11/26/2019
 * @email chriseteka@gmail.com
 */
@Service
public interface AuthenticationService<T> {

    T authenticateUser(T t);
}
