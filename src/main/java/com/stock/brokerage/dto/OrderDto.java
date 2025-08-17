package com.stock.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderDto {
    private int id;
    private int customerId;
    private String assetName;
    private int orderSide;
    private String orderSideDescription;
    private BigDecimal size;
    private BigDecimal price;
    private int status;
    private String statusDescription;
    private LocalDateTime createDate;
    public OrderDto(int id, int customerId, String assetName, int orderSide, String orderSideDescription, BigDecimal size, BigDecimal price, int status, String statusDescription, LocalDateTime createDate) {
        this.id = id;
        this.customerId = customerId;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.orderSideDescription = orderSideDescription;
        this.size = size;
        this.price = price;
        this.status = status;
        this.statusDescription = statusDescription;
        this.createDate = createDate;
    }
}
