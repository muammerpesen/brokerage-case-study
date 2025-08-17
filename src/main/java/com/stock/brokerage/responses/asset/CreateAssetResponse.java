package com.stock.brokerage.responses.asset;

import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.responses.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAssetResponse extends BaseResponse {
    Asset asset;
}
