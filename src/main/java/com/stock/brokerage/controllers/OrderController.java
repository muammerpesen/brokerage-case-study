package com.stock.brokerage.controllers;

import com.stock.brokerage.auth.Authz;
import com.stock.brokerage.requests.order.CreateOrderRequest;
import com.stock.brokerage.requests.order.DeleteOrderRequest;
import com.stock.brokerage.requests.order.ListOrderRequest;
import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.order.CreateOrderResponse;
import com.stock.brokerage.responses.order.DeleteOrderResponse;
import com.stock.brokerage.responses.order.ListOrderResponse;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping(value = "/order/v1")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(path = "/create")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request, Authentication auth) throws AccessDeniedException {
        return new ResponseEntity<>(orderService.createOrder(request, auth), HttpStatus.OK);
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<DeleteOrderResponse> deleteOrder(@RequestBody DeleteOrderRequest request, Authentication auth) {
        return new ResponseEntity<>(orderService.deleteOrder(request, auth), HttpStatus.OK);
    }

    @PostMapping(path = "/match")
    public ResponseEntity<MatchOrderResponse> matchOrder(@RequestBody MatchOrderRequest request, Authentication auth) {
        return new ResponseEntity<>(orderService.matchOrder(request, auth), HttpStatus.OK);
    }

    @PostMapping(path = "/list")
    public ResponseEntity<ListOrderResponse> listOrder(@RequestBody ListOrderRequest request, Authentication auth) throws AccessDeniedException {
        return new ResponseEntity<>(orderService.listOrder(request, auth), HttpStatus.OK);
    }
}
