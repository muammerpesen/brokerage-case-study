package com.stock.brokerage.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter
@Setter
public class AssetDto {
    private int id;
    private int customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
    public AssetDto(int id, int customerId, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.id = id;
        this.customerId = customerId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }
    public AssetDto() {

    }
}
