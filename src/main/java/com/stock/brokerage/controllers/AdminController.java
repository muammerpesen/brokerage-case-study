package com.stock.brokerage.controllers;

import com.stock.brokerage.requests.order.MatchOrderRequest;
import com.stock.brokerage.responses.order.MatchOrderResponse;
import com.stock.brokerage.services.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin/v1")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(path = "/match")
    public ResponseEntity<MatchOrderResponse> matchOrder(@RequestBody MatchOrderRequest request, Authentication auth) {
        return new ResponseEntity<>(adminService.matchOrder(request, auth), HttpStatus.OK);
    }
}
