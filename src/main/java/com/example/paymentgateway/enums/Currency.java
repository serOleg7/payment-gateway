package com.example.paymentgateway.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.EnumUtils;

public enum Currency {
    EUR, USD;

    @JsonCreator
    public static Currency getCurrency(String str) {
        return EnumUtils.isValidEnumIgnoreCase(Currency.class, str) ? Currency.valueOf(str.toUpperCase()) : null;
    }
}

