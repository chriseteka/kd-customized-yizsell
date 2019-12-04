package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
@Getter
@AllArgsConstructor
public abstract class InventoryAPIExceptions extends RuntimeException {

    private final String defaultMessageCode;

    private final String defaultUserMessage;

    private final Object[] defaultUserMessageArguments;
}
