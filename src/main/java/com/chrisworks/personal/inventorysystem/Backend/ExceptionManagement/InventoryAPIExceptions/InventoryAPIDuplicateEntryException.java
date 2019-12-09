package com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions;

/**
 * @author Chris_Eteka
 * @since 12/7/2019
 * @email chriseteka@gmail.com
 */
public class InventoryAPIDuplicateEntryException extends InventoryAPIExceptions {

    public InventoryAPIDuplicateEntryException(String defaultMessageCode, String defaultUserMessage, Object[] defaultUserMessageArguments) {
        super(defaultMessageCode, defaultUserMessage, defaultUserMessageArguments);
    }
}
