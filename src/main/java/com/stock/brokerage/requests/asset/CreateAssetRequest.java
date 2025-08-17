package com.stock.brokerage.requests.asset;

import com.stock.brokerage.requests.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CreateAssetRequest extends BaseRequest {
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;

}
