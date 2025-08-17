package com.stock.brokerage.services;

import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.entities.Order;
import com.stock.brokerage.repositories.OrderRepository;
import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.utilities.Util;
import com.stock.brokerage.utilities.enums.CurrencyEnum;
import com.stock.brokerage.utilities.enums.OrderSideEnum;
import com.stock.brokerage.utilities.enums.OrderStatusEnum;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    OrderRepository orderRepository;
    OrderService orderService;
    AssetService assetService;

    public AdminService(OrderRepository orderRepository, OrderService orderService, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.assetService = assetService;
    }

    @Transactional
    @PreAuthorize("@authz.checkAuthByOrderId(authentication, #request.id)")
    public MatchOrderResponse matchOrder(MatchOrderRequest request, Authentication auth) {
        MatchOrderResponse matchOrderResponse = new MatchOrderResponse();
        List<Asset> assetList = new ArrayList<>();

        try {
            Optional<Order> orderOptional = orderRepository.findById(request.getId());

            if (orderOptional.isEmpty()) {
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "admin.matchOrder.orderisempty"));
                return matchOrderResponse;
            }

            Order order = orderOptional.get();
            if (OrderStatusEnum.PENDING.getId() != order.getStatus()) {
                matchOrderResponse.setSuccess(false);
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "admin.matchOrder.matcherror"));
                return matchOrderResponse;
            }

            order.setStatus(OrderStatusEnum.MATCHED.getId());
            orderService.saveOrder(order);

            Asset assetTRY = assetService.getAsset(order.getCustomerId(), CurrencyEnum.TRY.getId());
            Asset asset = assetService.getAsset(order.getCustomerId(), order.getAssetName());

            if(assetTRY == null) {
                matchOrderResponse.setSuccess(false);
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "admin.matchOrder.assetTRYnotfound"));
                return matchOrderResponse;

            }

            if(asset == null) {
                matchOrderResponse.setSuccess(false);
                matchOrderResponse.setAdditionalInfo(Util.getMessage("message", "admin.matchOrder.assetnotfound"));
                return matchOrderResponse;
            }

            if (order.getOrderSide() == OrderSideEnum.BUY.getId()) {
                assetList = setAssetListForBuying(order, assetTRY, asset);
                assetService.saveAsset(assetList);
            }
            else if (order.getOrderSide() == OrderSideEnum.SELL.getId()) {
                if (order.getSize().compareTo(asset.getUsableSize()) > 0) {
                    BigDecimal addedSize = order.getSize().subtract(asset.getUsableSize());
                    asset.setSize(asset.getSize().add(addedSize));
                    asset.setUsableSize(asset.getUsableSize().add(addedSize));
                    assetService.saveAsset(asset);
                }
            }

        } catch (Exception exception) {
            matchOrderResponse.setSuccess(false);
            matchOrderResponse.setAdditionalInfo(exception.getMessage());
        }

        return matchOrderResponse;
    }

    private List<Asset> setAssetListForBuying(Order order, Asset assetTRY, Asset asset) {
        List<Asset> assetList = new ArrayList<>();
        BigDecimal cost = order.getPrice().multiply(order.getSize());

        if (cost.compareTo(assetTRY.getUsableSize()) > 0) {
            BigDecimal addedCost = cost.subtract(assetTRY.getUsableSize());
            assetTRY.setUsableSize(assetTRY.getUsableSize().add(addedCost));
            assetTRY.setSize(assetTRY.getSize().add(addedCost));
            assetList.add(assetTRY);
        }

        asset.setCustomerId(order.getCustomerId());
        asset.setAssetName(order.getAssetName());
        asset.setSize(order.getSize());
        asset.setUsableSize(order.getSize());
        assetList.add(asset);
        return assetList;
    }

}
