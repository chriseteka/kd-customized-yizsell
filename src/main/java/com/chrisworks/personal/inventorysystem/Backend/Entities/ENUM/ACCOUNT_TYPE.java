package com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM;

import java.util.stream.Stream;

public enum ACCOUNT_TYPE {

    BUSINESS_OWNER(100),

    WAREHOUSE_ATTENDANT(200),

    SHOP_SELLER(300),

    STAFF(400);

    private int account_type_value;

    ACCOUNT_TYPE(int account_type_value) {
        this.account_type_value = account_type_value;
    }

    public int getAccount_type_value() {
        return account_type_value;
    }

    public static ACCOUNT_TYPE of(int account_type_value) {
        return Stream.of(ACCOUNT_TYPE.values())
                .filter(p -> p.getAccount_type_value() == account_type_value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
