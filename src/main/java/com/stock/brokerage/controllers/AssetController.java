package com.stock.brokerage.controllers;

import com.stock.brokerage.requests.asset.CreateAssetRequest;
import com.stock.brokerage.requests.asset.ListAssetRequest;
import com.stock.brokerage.responses.asset.CreateAssetResponse;
import com.stock.brokerage.responses.asset.ListAssetResponse;
import com.stock.brokerage.services.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping(value = "/asset/v1")
public class AssetController {
    public AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(path = "/list")
    public ResponseEntity<ListAssetResponse> listAsset(@RequestBody ListAssetRequest request, Authentication auth) throws AccessDeniedException {
        return new ResponseEntity<>(assetService.listAsset(request, auth), HttpStatus.OK);
    }
}
