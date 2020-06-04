package com.chrisworks.personal.inventorysystem.Backend.Configurations;

public class SecurityConstants {

    static final String SIGN_UP_URL = "/BO/createAccount";
    static final String SIGN_IN_URL = "/auth/*";
    static final String CHAT_URL = "/chat/**";
    public static final String TOKEN_PREFIX = "Bearer ";
    static final String HEADER_STRING = "Authorization";
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    public static final String POST_METHOD = "POST";
    public static final String PUT_METHOD = "PUT";
    public static final String URI_KEY = "uriKey";
}
