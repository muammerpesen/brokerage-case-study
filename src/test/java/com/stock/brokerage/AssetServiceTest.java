package com.stock.brokerage;

import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.repositories.AssetRepository;
import com.stock.brokerage.requests.asset.ListAssetRequest;
import com.stock.brokerage.responses.asset.ListAssetResponse;
import com.stock.brokerage.services.AssetService;
import com.stock.brokerage.utilities.Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {
    @Mock
    AssetRepository assetRepository;
    @InjectMocks
    AssetService assetService;

    @Test
    void listService_test() {
        // given
        ListAssetRequest req = new ListAssetRequest();
        req.setCustomerId(101);

        Asset a = new Asset();
        a.setCustomerId(101);
        a.setAssetName("TRY");
        a.setSize(new BigDecimal("100"));
        a.setUsableSize(new BigDecimal("100"));
        List<Asset> assets = List.of(a);

        when(assetRepository.findByCustomerId(101)).thenReturn(assets);

        var dto = new com.stock.brokerage.dto.AssetDto();
        dto.setAssetName("TRY");
        dto.setSize(new BigDecimal("100"));
        dto.setUsableSize(new BigDecimal("100"));
        List<com.stock.brokerage.dto.AssetDto> dtos = List.of(dto);

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.assetToAssetDto(assets)).thenReturn(dtos);
            ListAssetResponse resp = assetService.listAsset(req, null);
            Assertions.assertNotNull(resp);
            Assertions.assertTrue(resp.isSuccess());
            Assertions.assertNotNull(resp.getAssetDtoList());
            Assertions.assertEquals(1, resp.getAssetDtoList().size());
            Assertions.assertEquals("TRY", resp.getAssetDtoList().get(0).getAssetName());
            verify(assetRepository).findByCustomerId(101);
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAsset_test() {
        int customerId = 101;

        Asset a1 = new Asset();
        a1.setId(1);
        a1.setCustomerId(customerId);
        a1.setAssetName("TRY");

        Asset a2 = new Asset();
        a2.setId(2);
        a2.setCustomerId(customerId);
        a2.setAssetName("BTC");

        when(assetRepository.findByCustomerId(customerId)).thenReturn(List.of(a1, a2));
        Asset result = assetService.getAsset(customerId);
        assertThat(result).isSameAs(a1);
        verify(assetRepository).findByCustomerId(customerId);
    }

    @Test
    void getAsset_Empty() {
        int customerId = 101;
        when(assetRepository.findByCustomerId(customerId)).thenReturn(List.of());
        Asset result = assetService.getAsset(customerId);
        assertThat(result).isNotNull();
        verify(assetRepository).findByCustomerId(customerId);
    }
}
