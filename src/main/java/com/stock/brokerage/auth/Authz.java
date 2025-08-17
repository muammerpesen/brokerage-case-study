package com.stock.brokerage.auth;

import com.stock.brokerage.entities.Order;
import com.stock.brokerage.repositories.CustomerRepository;
import com.stock.brokerage.repositories.OrderRepository;
import com.stock.brokerage.utilities.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Component("authz")
@RequiredArgsConstructor
public class Authz {
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public boolean checkAuthByCustomerId(Authentication auth, Integer requestedCustomerId) throws AccessDeniedException {
        var customer = customerRepository.findByUsername(auth.getName()).orElseThrow();
        if (customer.getRole() == RoleEnum.ADMIN)
            return true;
        if (!requestedCustomerId.equals(customer.getId()))
            return false;
        else
            return true;
    }

    public boolean checkAuthByOrderId(Authentication auth, Integer id) throws AccessDeniedException {
        var customer = customerRepository.findByUsername(auth.getName()).orElseThrow();
        if (customer.getRole() == RoleEnum.ADMIN)
            return true;

        Optional<Order> optionalOrder = orderRepository.findById(id);
        if(optionalOrder.isEmpty())
            return true;

        if (optionalOrder.get().getCustomerId() != customer.getId())
            return false;
        else
            return true;
    }
    public boolean checkIsAdmin(Authentication auth, Integer id) throws AccessDeniedException {
        var customer = customerRepository.findByUsername(auth.getName()).orElseThrow();
        if (customer.getRole() == RoleEnum.ADMIN)
            return true;
        else
            return false;
    }
}