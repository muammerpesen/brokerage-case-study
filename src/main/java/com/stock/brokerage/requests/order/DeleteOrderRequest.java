package com.stock.brokerage.requests.order;

import com.stock.brokerage.requests.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteOrderRequest {
    private int id;
}
