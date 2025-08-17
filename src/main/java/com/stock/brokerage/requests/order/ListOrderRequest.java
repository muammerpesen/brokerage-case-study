package com.stock.brokerage.requests.order;

import com.stock.brokerage.requests.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ListOrderRequest extends BaseRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
