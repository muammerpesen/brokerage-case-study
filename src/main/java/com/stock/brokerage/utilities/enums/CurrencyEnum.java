package com.stock.brokerage.utilities.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum CurrencyEnum {
    TRY("TRY", "TRY");
    private String id;
    private String description;
    CurrencyEnum(String id, String description) {
        this.id = id;
        this.description = description;
    }

}
