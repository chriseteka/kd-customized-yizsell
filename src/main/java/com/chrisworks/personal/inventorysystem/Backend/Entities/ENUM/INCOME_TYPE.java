package com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM;

import java.util.stream.Stream;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public enum INCOME_TYPE {

    STOCK_SALE(100),

    DEBT_CLEARANCE(200),

    OTHERS(300);

    private int income_type_value;

    INCOME_TYPE(int income_type_value) {
        this.income_type_value = income_type_value;
    }

    public int getIncome_type_value() {
        return income_type_value;
    }

    public static INCOME_TYPE of(int income_type_value) {
        return Stream.of(INCOME_TYPE.values())
                .filter(p -> p.getIncome_type_value() == income_type_value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
