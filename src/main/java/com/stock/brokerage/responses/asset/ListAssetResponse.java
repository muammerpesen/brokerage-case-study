package com.stock.brokerage.responses.asset;

import com.stock.brokerage.dto.AssetDto;
import com.stock.brokerage.responses.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListAssetResponse extends BaseResponse {
    private List<AssetDto> assetDtoList;
}
