package com.chrisworks.personal.inventorysystem.Backend.Configurations;

public class SecurityConstants {

    public static final String SIGN_UP_URL = "/BO/createAccount";
    public static final String SIGN_IN_URL = "/auth/*";
    public static final String CHAT_URL = "/chat/**";
    public static final String SECRET = "InventoryAPI53271514Secret";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final Long TOKEN_EXPIRATION_TIME = Long.parseLong("259200000"); // 3days
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;
}
