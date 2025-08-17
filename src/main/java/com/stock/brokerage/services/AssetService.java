package com.stock.brokerage.services;

import com.stock.brokerage.auth.Authz;
import com.stock.brokerage.dto.AssetDto;
import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.repositories.AssetRepository;
import com.stock.brokerage.requests.asset.CreateAssetRequest;
import com.stock.brokerage.requests.asset.ListAssetRequest;
import com.stock.brokerage.responses.asset.CreateAssetResponse;
import com.stock.brokerage.responses.asset.ListAssetResponse;
import com.stock.brokerage.utilities.Util;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.PresentationDirection;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class AssetService {
    private AssetRepository assetRepository;
    private final Authz authz;

    public AssetService(AssetRepository assetRepository, Authz authz) {
        this.authz = authz;
        this.assetRepository = assetRepository;
    }

    public CreateAssetResponse createAsset(CreateAssetRequest request) {
        CreateAssetResponse createAssetResponse = new CreateAssetResponse();

        try {
            Asset asset = new Asset();
            asset.setCustomerId(request.getCustomerId());
            asset.setAssetName(request.getAssetName());
            asset.setSize(request.getSize());
            asset.setUsableSize(request.getUsableSize());
            createAssetResponse.setAsset(saveAsset(asset));

        } catch (Exception exception) {
            createAssetResponse.setSuccess(false);
            createAssetResponse.setAdditionalInfo(exception.getMessage());
        }

        return createAssetResponse;
    }
    @PreAuthorize("@authz.checkAuthByCustomerId(authentication, #request.customerId)")
    public ListAssetResponse listAsset(ListAssetRequest request, Authentication auth) throws AccessDeniedException {
        ListAssetResponse response = new ListAssetResponse();

        try {
            List<Asset> assetList = assetRepository.findByCustomerId(request.getCustomerId());
            List<AssetDto> assetDtoList = Util.assetToAssetDto(assetList);
            response.setAssetDtoList(assetDtoList);

        } catch (Exception exception) {
            response.setSuccess(false);
            response.setAdditionalInfo(exception.getMessage());
        }

        return response;
    }
    public Asset getAsset(int customerId) {
        List<Asset> assetList = assetRepository.findByCustomerId(customerId);
        return assetList.isEmpty() ? new Asset() : assetList.get(0);
    }
    public Asset getAsset(int customerId, String assetName) {
        List<Asset> assetList = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
        return assetList.isEmpty() ? null : assetList.get(0);
    }
    public Asset saveAsset(Asset asset) {
        return assetRepository.save(asset);
    }
    public List<Asset> saveAsset(List<Asset> assetList) {
        return assetRepository.saveAll(assetList);
    }

}
