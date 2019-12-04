package com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM;

import java.util.stream.Stream;

/**
 * @author Chris_Eteka
 * @since 11/25/2019
 * @email chriseteka@gmail.com
 */
public enum PAYMENT_MODE {

    CASH(100),

    TRANSFER(200),

    BANK_DEPOSIT(300);

    private int payment_mode_value;

    PAYMENT_MODE(int payment_mode_value) {
        this.payment_mode_value = payment_mode_value;
    }

    public int getPayment_mode_value() {
        return payment_mode_value;
    }

    public static PAYMENT_MODE of(int payment_mode_value) {
        return Stream.of(PAYMENT_MODE.values())
                .filter(p -> p.getPayment_mode_value() == payment_mode_value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
