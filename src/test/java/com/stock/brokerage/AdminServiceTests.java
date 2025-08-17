package com.stock.brokerage;


import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.entities.Order;
import com.stock.brokerage.repositories.OrderRepository;
import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.services.AdminService;
import com.stock.brokerage.services.AssetService;
import com.stock.brokerage.services.OrderService;
import com.stock.brokerage.utilities.Util;
import com.stock.brokerage.utilities.enums.CurrencyEnum;
import com.stock.brokerage.utilities.enums.OrderSideEnum;
import com.stock.brokerage.utilities.enums.OrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTests {
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderService orderService;
    @Mock
    AssetService assetService;

    @InjectMocks
    AdminService adminService;

    @Test
    void matchOrder_returnsMessage_whenOrderNotFound() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(123);

        when(orderRepository.findById(123)).thenReturn(Optional.empty());

        try (MockedStatic<Util> util = mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "admin.matchOrder.orderisempty")) .thenReturn("empty");
            MatchOrderResponse resp = adminService.matchOrder(req, null);
            assertThat(resp.getAdditionalInfo()).isEqualTo("empty");
            verifyNoInteractions(orderService, assetService);
        }
    }

    @Test
    void matchOrder_returnsError_whenOrderIsNotPending() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(1);

        Order order = new Order();
        order.setId(1);
        order.setStatus(OrderStatusEnum.MATCHED.getId());

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        try (MockedStatic<Util> util = mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "admin.matchOrder.matcherror")).thenReturn("matcherror");
            MatchOrderResponse resp = adminService.matchOrder(req, null);
            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("matcherror");
            verify(orderService, never()).saveOrder(any());
            verifyNoInteractions(assetService);
        }
    }

    @Test
    void matchOrder_returnsError_whenTryAssetIsNull() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(7);

        Order order = pendingBuyOrder(7, 101, "deneme", 10, "100");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(assetService.getAsset(101, CurrencyEnum.TRY.getId())).thenReturn(null);

        try (MockedStatic<Util> util = mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "admin.matchOrder.assetTRYnotfound")).thenReturn("assetTRYnotfound");
            MatchOrderResponse resp = adminService.matchOrder(req, null);
            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("assetTRYnotfound");
            verify(orderService).saveOrder(order);
            verify(assetService, never()).saveAsset(any(List.class));
        }
    }

    @Test
    void matchOrder_returnsError_whenTargetAssetIsNull() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(8);

        Order order = pendingBuyOrder(8, 101, "deneme", 10, "100");
        when(orderRepository.findById(8)).thenReturn(Optional.of(order));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(101);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal("500"));
        tryAsset.setUsableSize(new BigDecimal("500"));

        when(assetService.getAsset(101, CurrencyEnum.TRY.getId())).thenReturn(tryAsset);
        when(assetService.getAsset(101, "deneme")).thenReturn(null);

        try (MockedStatic<Util> util = mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "admin.matchOrder.assetnotfound")).thenReturn("assetnotfound");
            MatchOrderResponse resp = adminService.matchOrder(req, null);
            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("assetnotfound");
            verify(orderService).saveOrder(order);
            verify(assetService, never()).saveAsset(any(List.class));
        }
    }

    @Test
    void matchOrder_buy_withTopUp_savesTwoAssets_withCurrentImplementation() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(9);

        Order order = pendingBuyOrder(9, 101, "BTC", 10, "100");
        when(orderRepository.findById(9)).thenReturn(Optional.of(order));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(101);
        tryAsset.setAssetName("TRY");
        tryAsset.setSize(new BigDecimal("500"));
        tryAsset.setUsableSize(new BigDecimal("500"));

        Asset target = new Asset();
        target.setCustomerId(101);
        target.setAssetName("BTC");
        target.setSize(new BigDecimal("2"));
        target.setUsableSize(new BigDecimal("2"));

        when(assetService.getAsset(101, CurrencyEnum.TRY.getId())).thenReturn(tryAsset);
        when(assetService.getAsset(101, "BTC")).thenReturn(target);

        ArgumentCaptor<List<Asset>> listCaptor = ArgumentCaptor.forClass(List.class);

        MatchOrderResponse resp = adminService.matchOrder(req, null);

        verify(orderService).saveOrder(order);
        verify(assetService).saveAsset(listCaptor.capture());

        List<Asset> saved = listCaptor.getValue();
        assertThat(saved).hasSize(2);

        Asset savedTry   = saved.stream().filter(a -> "TRY".equals(a.getAssetName())).findFirst().orElseThrow();
        Asset savedBTC  = saved.stream().filter(a -> "BTC".equals(a.getAssetName())).findFirst().orElseThrow();

        assertThat(savedTry.getSize()).isEqualByComparingTo("1000");
        assertThat(savedTry.getUsableSize()).isEqualByComparingTo("1000");

        assertThat(savedBTC.getSize()).isEqualByComparingTo("10");
        assertThat(savedBTC.getUsableSize()).isEqualByComparingTo("10");

        assertThat(resp).isNotNull();
    }

    @Test
    void matchOrder_Sell() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(10);

        Order order = pendingSellOrder(10, 101, "BTC", 10, "100");
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(101);
        tryAsset.setAssetName("TRY");

        Asset target = new Asset();
        target.setCustomerId(101);
        target.setAssetName("BTC");
        target.setSize(new BigDecimal("3"));
        target.setUsableSize(new BigDecimal("3"));

        when(assetService.getAsset(101, CurrencyEnum.TRY.getId())).thenReturn(tryAsset);
        when(assetService.getAsset(101, "BTC")).thenReturn(target);

        MatchOrderResponse resp = adminService.matchOrder(req, null);
        verify(orderService).saveOrder(order);
        assertThat(resp).isNotNull();
    }
    private static Order pendingBuyOrder(int id, int customerId, String assetName, int size, String price) {
        Order o = new Order();
        o.setId(id);
        o.setCustomerId(customerId);
        o.setAssetName(assetName);
        o.setOrderSide(OrderSideEnum.BUY.getId());
        o.setStatus(OrderStatusEnum.PENDING.getId());
        o.setSize(new BigDecimal(String.valueOf(size)));
        o.setPrice(new BigDecimal(price));
        return o;
    }

    private static Order pendingSellOrder(int id, int customerId, String assetName, int size, String price) {
        Order o = new Order();
        o.setId(id);
        o.setCustomerId(customerId);
        o.setAssetName(assetName);
        o.setOrderSide(OrderSideEnum.SELL.getId());
        o.setStatus(OrderStatusEnum.PENDING.getId());
        o.setSize(new BigDecimal(String.valueOf(size)));
        o.setPrice(new BigDecimal(price));
        return o;
    }

    private static Asset asset(String name, int size, int usable) {
        Asset a = new Asset();
        a.setAssetName(name);
        a.setSize(new BigDecimal(String.valueOf(size)));
        a.setUsableSize(new BigDecimal(String.valueOf(usable)));
        return a;
    }
}
