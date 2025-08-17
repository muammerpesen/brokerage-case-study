package com.stock.brokerage.responses.order;

import com.stock.brokerage.dto.OrderDto;
import com.stock.brokerage.responses.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ListOrderResponse extends BaseResponse {
    List<OrderDto> orderDtoList;
}
