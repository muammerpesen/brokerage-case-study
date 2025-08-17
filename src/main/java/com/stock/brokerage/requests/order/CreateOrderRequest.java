package com.stock.brokerage.requests.order;

import com.stock.brokerage.requests.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderRequest extends BaseRequest {
    private String assetName;
    private int orderSide;
    private BigDecimal size;
    private BigDecimal price;
}