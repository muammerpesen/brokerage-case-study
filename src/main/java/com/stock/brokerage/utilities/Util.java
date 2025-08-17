package com.stock.brokerage.utilities;

import com.stock.brokerage.dto.AssetDto;
import com.stock.brokerage.dto.OrderDto;
import com.stock.brokerage.entities.Asset;
import com.stock.brokerage.entities.Order;
import com.stock.brokerage.utilities.enums.OrderSideEnum;
import com.stock.brokerage.utilities.enums.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class Util {

    public static List<OrderDto> orderToOrderDto(List<Order> orderList) {
        List<OrderDto> orderDtoList = orderList.stream()
                .map(order -> new OrderDto(order.getId(),
                        order.getCustomerId(),
                        order.getAssetName(),
                        order.getOrderSide(),
                        OrderSideEnum.fromId(order.getOrderSide()).getDescription(),
                        order.getSize(),
                        order.getPrice(),
                        order.getStatus(),
                        OrderStatusEnum.fromId(order.getStatus()).getDescription(),
                        order.getCreateDate()

                ))
                .collect(Collectors.toList());

        return orderDtoList;
    }

    public static List<AssetDto> assetToAssetDto(List<Asset> assetList) {
        List<AssetDto> assetDtoList = assetList.stream()
                .map(asset -> new AssetDto(asset.getId(),
                        asset.getCustomerId(),
                        asset.getAssetName(),
                        asset.getSize(),
                        asset.getUsableSize())).collect(Collectors.toList());

        return assetDtoList;
    }

    public static String getMessage(String properties, String key) {
        ResourceBundleMessageSource messageSource = messageSource(properties);
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    public static String getMessage(String properties, String key, String obj) {
        ResourceBundleMessageSource messageSource = messageSource(properties);
        return messageSource.getMessage(key, new Object[]{obj}, Locale.ENGLISH);
    }

    public static ResourceBundleMessageSource messageSource(String properties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(properties);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
