package com.stock.brokerage.services;

import com.stock.brokerage.auth.Authz;
import com.stock.brokerage.dto.OrderDto;
import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.entities.Order;
import com.stock.brokerage.repositories.OrderRepository;
import com.stock.brokerage.requests.order.CreateOrderRequest;
import com.stock.brokerage.requests.order.DeleteOrderRequest;
import com.stock.brokerage.requests.order.ListOrderRequest;
import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.BaseResponse;
import com.stock.brokerage.responses.order.CreateOrderResponse;
import com.stock.brokerage.responses.order.DeleteOrderResponse;
import com.stock.brokerage.responses.order.ListOrderResponse;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.utilities.Util;
import com.stock.brokerage.utilities.enums.CurrencyEnum;
import com.stock.brokerage.utilities.enums.OrderSideEnum;
import com.stock.brokerage.utilities.enums.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private OrderRepository orderRepository;
    private AssetService assetService;
    private final Authz authz;

    public OrderService(OrderRepository orderRepository, Authz authz, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.authz = authz;
        this.assetService = assetService;
    }
    @Transactional
    @PreAuthorize("@authz.checkAuthByCustomerId(authentication, #request.customerId)")
    public CreateOrderResponse createOrder(CreateOrderRequest request, Authentication auth) throws AccessDeniedException {

        CreateOrderResponse createOrderResponse = new CreateOrderResponse();

        try {
            if (!(OrderSideEnum.BUY.getId() == request.getOrderSide() || OrderSideEnum.SELL.getId() == request.getOrderSide())) {
                createOrderResponse.setSuccess(false);
                createOrderResponse.setAdditionalInfo(Util.getMessage("message", "order.createOrder.undefinedsidetype"));
                return createOrderResponse;
            }

            BigDecimal holdAmount = BigDecimal.ZERO;
            String assetName = "";

            if (OrderSideEnum.BUY.getId() == request.getOrderSide()) {
                assetName = CurrencyEnum.TRY.getId();
                holdAmount = request.getSize().multiply(request.getPrice());
            } else if (OrderSideEnum.SELL.getId() == request.getOrderSide()) {
                assetName = request.getAssetName();
                holdAmount = request.getSize();
            }

            BaseResponse orderBuyOrSellResponse = orderBuyOrSell(request, holdAmount, assetName);
            if (!orderBuyOrSellResponse.isSuccess()) {
                createOrderResponse.setSuccess(orderBuyOrSellResponse.isSuccess());
                createOrderResponse.setAdditionalInfo(orderBuyOrSellResponse.getAdditionalInfo());
                return createOrderResponse;
            }

            Order order = new Order();
            order.setCustomerId(request.getCustomerId());
            order.setAssetName(request.getAssetName());
            order.setOrderSide(request.getOrderSide());
            order.setSize(request.getSize());
            order.setPrice(request.getPrice());
            order.setStatus(OrderStatusEnum.PENDING.getId());
            order.setCreateDate(LocalDateTime.now());
            createOrderResponse.setOrder(saveOrder(order));
        } catch (Exception exception) {
            createOrderResponse.setSuccess(false);
            createOrderResponse.setAdditionalInfo(exception.getMessage());
        }

        return createOrderResponse;
    }
    @Transactional
    @PreAuthorize("@authz.checkAuthByOrderId(authentication, #request.id)")
    public DeleteOrderResponse deleteOrder(DeleteOrderRequest request, Authentication auth) {
        DeleteOrderResponse deleteOrderResponse = new DeleteOrderResponse();

        try {
            Optional<Order> optionalOrder = orderRepository.findById(request.getId());

            if (optionalOrder.isEmpty()) {
                deleteOrderResponse.setAdditionalInfo(Util.getMessage("message", "order.deleteOrder.orderisempty"));
                return deleteOrderResponse;
            }

            Order order = optionalOrder.get();
            if (OrderStatusEnum.PENDING.getId() != order.getStatus()) {
                deleteOrderResponse.setSuccess(false);
                deleteOrderResponse.setAdditionalInfo(Util.getMessage("message", "order.matchOrder.deleteerror"));
                return deleteOrderResponse;
            }

            order.setStatus(OrderStatusEnum.CANCELED.getId());
            saveOrder(order);

            if (order.getOrderSide() == OrderSideEnum.BUY.getId()) {
                Asset asset = assetService.getAsset(order.getCustomerId(), CurrencyEnum.TRY.getId());
                asset.setUsableSize(asset.getUsableSize().add(order.getPrice().multiply(order.getSize())));
                assetService.saveAsset(asset);
            } else if (order.getOrderSide() == OrderSideEnum.SELL.getId()) {
                Asset soldAsset = assetService.getAsset(order.getCustomerId(), order.getAssetName());
                soldAsset.setUsableSize(soldAsset.getUsableSize().add(order.getSize()));
                assetService.saveAsset(soldAsset);
            }
        } catch (Exception exception) {
            deleteOrderResponse.setSuccess(false);
            deleteOrderResponse.setAdditionalInfo(exception.getMessage());
        }

        return deleteOrderResponse;
    }
    @Transactional
    @PreAuthorize("@authz.checkAuthByOrderId(authentication, #request.id)")
    public MatchOrderResponse matchOrder(MatchOrderRequest request, Authentication auth) {
        MatchOrderResponse matchOrderResponse = new MatchOrderResponse();

        try {
            Optional<Order> orderOptional = orderRepository.findById(request.getId());

            if (orderOptional.isEmpty()) {
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "order.matchOrder.orderisempty"));
                return matchOrderResponse;
            }

            Order order = orderOptional.get();

            if (OrderStatusEnum.PENDING.getId() != order.getStatus()) {
                matchOrderResponse.setSuccess(false);
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "order.matchOrder.deleteerror"));
                return matchOrderResponse;
            }

            order.setStatus(OrderStatusEnum.MATCHED.getId());
            saveOrder(order);

            if (order.getOrderSide() == OrderSideEnum.BUY.getId()) {
                Asset asset = assetService.getAsset(order.getCustomerId(), order.getAssetName());
                if(asset == null)
                    asset = new Asset();

                asset.setCustomerId(order.getCustomerId());
                asset.setAssetName(order.getAssetName());
                asset.setSize(order.getSize());
                asset.setUsableSize(order.getSize());
                assetService.saveAsset(asset);
            } else if (order.getOrderSide() == OrderSideEnum.SELL.getId()) {
                List<Asset> assetList = new ArrayList<>();
                Asset assetTRY = assetService.getAsset(order.getCustomerId(), CurrencyEnum.TRY.getId());

                if(assetTRY != null) {
                    assetTRY.setUsableSize(assetTRY.getUsableSize().add(order.getSize().multiply(order.getPrice())));
                    assetList.add(assetTRY);
                }

                assetService.saveAsset(assetTRY);
            }
        } catch (Exception exception) {
            matchOrderResponse.setSuccess(false);
            matchOrderResponse.setAdditionalInfo(exception.getMessage());
        }
        return matchOrderResponse;
    }
    @PreAuthorize("@authz.checkAuthByCustomerId(authentication, #request.customerId)")
    public ListOrderResponse listOrder(ListOrderRequest request, Authentication auth) throws AccessDeniedException {
        ListOrderResponse response = new ListOrderResponse();
        try {
            List<Order> orderList = orderRepository.findByCustomerIdAndCreateDateBetween(request.getCustomerId(),
                                                                                         request.getStartDate(),
                                                                                         request.getEndDate());

            List<OrderDto> orderDtoList = Util.orderToOrderDto(orderList);
            response.setOrderDtoList(orderDtoList);
        } catch (Exception exception) {
            response.setSuccess(false);
            response.setAdditionalInfo(exception.getMessage());
        }

        return response;
    }
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
    public BaseResponse orderBuyOrSell(CreateOrderRequest request, BigDecimal holdAmount, String currency) {
        BaseResponse response = new BaseResponse();
        Asset asset = assetService.getAsset(request.getCustomerId(), currency);
        if (asset != null) {
            if (asset.getUsableSize().compareTo(holdAmount) < 0) {
                response.setSuccess(false);
                response.setAdditionalInfo(Util.getMessage("message", "order.orderBuyOrSell.insufficientbalance"));
            }

            asset.setUsableSize(asset.getUsableSize().subtract(holdAmount));
            assetService.saveAsset(asset);
        }
        return response;

    }



}
