package com.stock.brokerage;

import com.stock.brokerage.auth.Authz;
import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.entities.Order;
import com.stock.brokerage.repositories.AssetRepository;
import com.stock.brokerage.repositories.OrderRepository;
import com.stock.brokerage.requests.order.CreateOrderRequest;
import com.stock.brokerage.requests.order.DeleteOrderRequest;
import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.BaseResponse;
import com.stock.brokerage.responses.order.CreateOrderResponse;
import com.stock.brokerage.responses.order.DeleteOrderResponse;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.services.AssetService;
import com.stock.brokerage.services.OrderService;
import com.stock.brokerage.utilities.Util;
import com.stock.brokerage.utilities.enums.CurrencyEnum;
import com.stock.brokerage.utilities.enums.OrderSideEnum;
import com.stock.brokerage.utilities.enums.OrderStatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {
    @Spy @InjectMocks
    private OrderService orderService;
    @Mock
    OrderRepository orderRepository;
    @Mock
    AssetRepository assetRepository;
    @Mock
    AssetService assetService;

    @Mock
    Authz authz;

    @Test
    void createOrder_sideIsInvalid() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(101);
        req.setAssetName("BTC");
        req.setOrderSide(999);
        req.setSize(new BigDecimal("10"));
        req.setPrice(new BigDecimal("100"));

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "order.createOrder.undefinedsidetype")).thenReturn("undefinedsidetype");

            CreateOrderResponse resp = orderService.createOrder(req, null);

            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("undefinedsidetype");
            verify(orderService, never()).orderBuyOrSell(any(), any(), any());
            verify(orderService, never()).saveOrder(any());
        }
    }

    @Test
    void createOrder_buy_happy_path() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(101);
        req.setAssetName("BTC");
        req.setOrderSide(OrderSideEnum.BUY.getId());
        req.setSize(new BigDecimal("10"));
        req.setPrice(new BigDecimal("100"));

        doReturn(new BaseResponse()).when(orderService)
                .orderBuyOrSell(eq(req), eq(new BigDecimal("1000")), eq(CurrencyEnum.TRY.getId()));

        Order saved = new Order();
        saved.setId(1);
        saved.setCustomerId(101);
        saved.setAssetName("BTC");
        saved.setOrderSide(OrderSideEnum.BUY.getId());
        saved.setSize(new BigDecimal("10"));
        saved.setPrice(new BigDecimal("100"));
        saved.setStatus(OrderStatusEnum.PENDING.getId());

        doReturn(saved).when(orderService).saveOrder(any(Order.class));
        CreateOrderResponse resp = orderService.createOrder(req, null);
        verify(orderService).orderBuyOrSell(eq(req), eq(new BigDecimal("1000")), eq(CurrencyEnum.TRY.getId()));

        ArgumentCaptor<Order> orderCap = ArgumentCaptor.forClass(Order.class);
        verify(orderService).saveOrder(orderCap.capture());
        Order toSave = orderCap.getValue();
        assertThat(toSave.getCustomerId()).isEqualTo(101);
        assertThat(toSave.getAssetName()).isEqualTo("BTC");
        assertThat(toSave.getOrderSide()).isEqualTo(OrderSideEnum.BUY.getId());
        assertThat(toSave.getSize()).isEqualByComparingTo("10");
        assertThat(toSave.getPrice()).isEqualByComparingTo("100");
        assertThat(toSave.getStatus()).isEqualTo(OrderStatusEnum.PENDING.getId());
        assertThat(toSave.getCreateDate()).isNotNull();

        assertThat(resp.getOrder()).isSameAs(saved);
    }

    @Test
    void createOrder_sell_happy_path() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(101);
        req.setAssetName("BTC");
        req.setOrderSide(OrderSideEnum.SELL.getId());
        req.setSize(new BigDecimal("7"));
        req.setPrice(new BigDecimal("123"));

        doReturn(new BaseResponse()).when(orderService)
                .orderBuyOrSell(eq(req), eq(new BigDecimal("7")), eq("BTC"));

        Order saved = new Order();
        saved.setId(2);
        doReturn(saved).when(orderService).saveOrder(any(Order.class));

        CreateOrderResponse resp = orderService.createOrder(req, null);

        verify(orderService).orderBuyOrSell(eq(req), eq(new BigDecimal("7")), eq("BTC"));
        verify(orderService).saveOrder(any(Order.class));
        assertThat(resp.getOrder()).isSameAs(saved);
    }

    @Test
    void createOrder_buyOrSellFails() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(101);
        req.setAssetName("BTC");
        req.setOrderSide(OrderSideEnum.BUY.getId());
        req.setSize(new BigDecimal("1"));
        req.setPrice(new BigDecimal("2"));

        BaseResponse fail = new BaseResponse();
        fail.setSuccess(false);
        fail.setAdditionalInfo("insufficient funds");

        doReturn(fail).when(orderService)
                .orderBuyOrSell(eq(req), eq(new BigDecimal("2")), eq(CurrencyEnum.TRY.getId()));

        CreateOrderResponse resp = orderService.createOrder(req, null);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getAdditionalInfo()).isEqualTo("insufficient funds");
        verify(orderService, never()).saveOrder(any());
    }

    @Test
    void deleteOrder_orderNotFound() {
        DeleteOrderRequest req = new DeleteOrderRequest();
        req.setId(111);

        when(orderRepository.findById(111)).thenReturn(Optional.empty());

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "order.deleteOrder.orderisempty"))
                    .thenReturn("orderisempty");

            DeleteOrderResponse resp = orderService.deleteOrder(req, null);

            assertThat(resp.getAdditionalInfo()).isEqualTo("orderisempty");
            verify(orderRepository).findById(111);
            verify(orderService, never()).saveOrder(any());
            verifyNoInteractions(assetService);
        }
    }

    @Test
    void deleteOrder_orderStatusIsNotPending() {
        DeleteOrderRequest req = new DeleteOrderRequest();
        req.setId(222);

        Order order = new Order();
        order.setId(222);
        order.setStatus(OrderStatusEnum.MATCHED.getId());

        when(orderRepository.findById(222)).thenReturn(Optional.of(order));

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "order.deleteordererror"))
                    .thenReturn("deleteordererror");

            DeleteOrderResponse resp = orderService.deleteOrder(req, null);

            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("deleteordererror");
            verify(orderService, never()).saveOrder(any());
            verifyNoInteractions(assetService);
        }
    }

    @Test
    void deleteOrder_buySetTRYUsableSize() {
        DeleteOrderRequest req = new DeleteOrderRequest();
        req.setId(333);

        Order order = new Order();
        order.setId(333);
        order.setCustomerId(101);
        order.setOrderSide(OrderSideEnum.BUY.getId());
        order.setStatus(OrderStatusEnum.PENDING.getId());
        order.setPrice(new BigDecimal("100"));
        order.setSize(new BigDecimal("2"));

        when(orderRepository.findById(333)).thenReturn(Optional.of(order));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(101);
        tryAsset.setAssetName("TRY");
        tryAsset.setUsableSize(new BigDecimal("500"));
        tryAsset.setSize(new BigDecimal("500"));

        when(assetService.getAsset(101, CurrencyEnum.TRY.getId())).thenReturn(tryAsset);
       // when(assetRepository.findByCustomerIdAndAssetName(101, "TRY")).thenReturn(java.util.List.of(tryAsset));

        doReturn(order).when(orderService).saveOrder(any(Order.class));

        DeleteOrderResponse resp = orderService.deleteOrder(req, null);

        ArgumentCaptor<Order> orderCap = ArgumentCaptor.forClass(Order.class);
        verify(orderService).saveOrder(orderCap.capture());
        assertThat(orderCap.getValue().getStatus()).isEqualTo(OrderStatusEnum.CANCELED.getId());

    }

    @Test
    void deleteOrder_sell_pending_increasesSoldAssetUsableBySize_andSaves() {
        DeleteOrderRequest req = new DeleteOrderRequest();
        req.setId(444);

        Order order = new Order();
        order.setId(444);
        order.setCustomerId(101);
        order.setOrderSide(OrderSideEnum.SELL.getId());
        order.setStatus(OrderStatusEnum.PENDING.getId());
        order.setAssetName("BTC");
        order.setSize(new BigDecimal("10"));

        when(orderRepository.findById(444)).thenReturn(Optional.of(order));

        Asset btc = new Asset();
        btc.setCustomerId(101);
        btc.setAssetName("BTC");
        btc.setUsableSize(new BigDecimal("3"));
        btc.setSize(new BigDecimal("3"));

        when(assetService.getAsset(101, "BTC")).thenReturn(btc);

        doReturn(order).when(orderService).saveOrder(any(Order.class));

        DeleteOrderResponse resp = orderService.deleteOrder(req, null);

        // Order CANCELED
        verify(orderService).saveOrder(argThat(o ->
                o.getStatus() == OrderStatusEnum.CANCELED.getId()
        ));


        assertThat(resp).isNotNull();
    }

    @Test
    void matchOrder_orderNotFound() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(999);

        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "order.matchOrder.orderisempty"))
                    .thenReturn("orderisempty");

            MatchOrderResponse resp = orderService.matchOrder(req, null);

            assertThat(resp.getAdditionalInfo()).isEqualTo("orderisempty");
            verify(orderRepository).findById(999);
            verify(orderRepository, never()).save(any());
            verifyNoInteractions(assetService);
        }
    }

    @Test
    void matchOrder_statusIsNotPending() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(7);

        Order order = new Order();
        order.setId(7);
        order.setStatus(OrderStatusEnum.CANCELED.getId()); // PENDING değil

        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        try (MockedStatic<Util> util = Mockito.mockStatic(Util.class)) {
            util.when(() -> Util.getMessage("message", "order.matchOrder.deleteerror"))
                    .thenReturn("deleteerror");

            MatchOrderResponse resp = orderService.matchOrder(req, null);

            assertThat(resp.isSuccess()).isFalse();
            assertThat(resp.getAdditionalInfo()).isEqualTo("deleteerror");
            verify(orderRepository, never()).save(any());
            verifyNoInteractions(assetService);
        }
    }

    @Test
    void matchOrder_buy() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(10);

        Order order = new Order();
        order.setId(10);
        order.setCustomerId(101);
        order.setAssetName("BTC");
        order.setOrderSide(OrderSideEnum.BUY.getId());
        order.setStatus(OrderStatusEnum.PENDING.getId());
        order.setSize(new BigDecimal("10"));
        order.setPrice(new BigDecimal("100"));

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Asset target = new Asset();
        target.setCustomerId(101);
        target.setAssetName("BTC");
        target.setSize(new BigDecimal("2"));
        target.setUsableSize(new BigDecimal("2"));

        when(assetService.getAsset(101, "BTC")).thenReturn(target);

        MatchOrderResponse resp = orderService.matchOrder(req, null);

        verify(orderRepository).save(argThat(o ->
                o.getId() == 10 && o.getStatus() == OrderStatusEnum.MATCHED.getId()
        ));

        verify(assetService).saveAsset(argThat((Asset a) ->
                "BTC".equals(a.getAssetName())
                        && a.getCustomerId() == 101
                        && a.getSize().compareTo(new BigDecimal("10")) == 0
                        && a.getUsableSize().compareTo(new BigDecimal("10")) == 0
        ));

        assertThat(resp).isNotNull();
    }

    @Test
    void matchOrder_buy_createsAssetWhenNull_andSaves() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(11);

        Order order = new Order();
        order.setId(11);
        order.setCustomerId(202);
        order.setAssetName("BTC");
        order.setOrderSide(OrderSideEnum.BUY.getId());
        order.setStatus(OrderStatusEnum.PENDING.getId());
        order.setSize(new BigDecimal("5"));
        order.setPrice(new BigDecimal("10"));

        when(orderRepository.findById(11)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(assetService.getAsset(202, "BTC")).thenReturn(null);

        MatchOrderResponse resp = orderService.matchOrder(req, null);

        verify(orderRepository).save(argThat(o ->
                o.getId() == 11 && o.getStatus() == OrderStatusEnum.MATCHED.getId()
        ));

        verify(assetService).saveAsset(argThat((Asset a) ->
                "BTC".equals(a.getAssetName())
                        && a.getCustomerId() == 202
                        && a.getSize().compareTo(new BigDecimal("5")) == 0
                        && a.getUsableSize().compareTo(new BigDecimal("5")) == 0
        ));

        assertThat(resp).isNotNull();
    }

    @Test
    void matchOrder_sell_increasesTRYUsableByCost_andSaves() {
        MatchOrderRequest req = new MatchOrderRequest();
        req.setId(12);

        Order order = new Order();
        order.setId(12);
        order.setCustomerId(303);
        order.setAssetName("BTC");
        order.setOrderSide(OrderSideEnum.SELL.getId());
        order.setStatus(OrderStatusEnum.PENDING.getId());
        order.setSize(new BigDecimal("3"));
        order.setPrice(new BigDecimal("200"));  // cost = 600

        when(orderRepository.findById(12)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Asset tryAsset = new Asset();
        tryAsset.setCustomerId(303);
        tryAsset.setAssetName("TRY");
        tryAsset.setUsableSize(new BigDecimal("500")); // 500 + 600 = 1100
        tryAsset.setSize(new BigDecimal("500"));

        when(assetService.getAsset(303, CurrencyEnum.TRY.getId())).thenReturn(tryAsset);

        MatchOrderResponse resp = orderService.matchOrder(req, null);

        // MATCHED kaydı
        verify(orderRepository).save(argThat(o ->
                o.getId() == 12 && o.getStatus() == OrderStatusEnum.MATCHED.getId()
        ));

        // TRY usable artışı doğrula
        verify(assetService).saveAsset(argThat((Asset a) ->
                "TRY".equals(a.getAssetName())
                        && a.getCustomerId() == 303
                        && a.getUsableSize().compareTo(new BigDecimal("1100")) == 0
        ));

        assertThat(resp).isNotNull();
    }

}
