package com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM;

import java.util.stream.Stream;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public enum EXPENSE_TYPE {

    PURCHASES(100),

    DEBT_CLEARANCE(200),

    RETURNED_SALE(300),

    STOCK_WAYBILL(400),

    SALE_REVERSAL(500),

    OTHERS(600);

    private int expense_type_value;

    EXPENSE_TYPE(int expense_type_value) {
        this.expense_type_value = expense_type_value;
    }

    public int getExpense_type_value() {
        return expense_type_value;
    }

    public static EXPENSE_TYPE of(int expense_type_value) {
        return Stream.of(EXPENSE_TYPE.values())
                .filter(p -> p.getExpense_type_value() == expense_type_value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
