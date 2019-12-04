package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions;

/**
 * @author Chris_Eteka
 * @since 12/4/2019
 * @email chriseteka@gmail.com
 */
public class InventoryAPIOperationException extends InventoryAPIExceptions {

    public InventoryAPIOperationException(String defaultMessageCode, String defaultUserMessage, Object[] defaultUserMessageArguments) {
        super(defaultMessageCode, defaultUserMessage, defaultUserMessageArguments);
    }
}
