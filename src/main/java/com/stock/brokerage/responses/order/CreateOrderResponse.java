package com.stock.brokerage.responses.order;

import com.stock.brokerage.entities.Order;
import com.stock.brokerage.responses.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderResponse extends BaseResponse {
    private Order order;
}
