package com.stock.brokerage.responses;

import com.stock.brokerage.utilities.constants.OrderConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {

    private boolean isSuccess;
    private String additionalInfo;
    public BaseResponse() {
        isSuccess = true;
        additionalInfo = OrderConstants.SUCCESS;
    }

    public BaseResponse(boolean isSuccess, String additionalInfo) {
        isSuccess = isSuccess;
        additionalInfo = additionalInfo;
    }

}
