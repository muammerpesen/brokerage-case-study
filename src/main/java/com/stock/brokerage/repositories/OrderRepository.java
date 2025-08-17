package com.stock.brokerage.repositories;

import com.stock.brokerage.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomerIdAndCreateDateBetween(int customerId, LocalDateTime startDate, LocalDateTime endDate);
}
