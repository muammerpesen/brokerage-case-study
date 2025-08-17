package com.stock.brokerage.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    public Order() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int customerId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false)
    private int orderSide;

    @Column(nullable = false, precision=19, scale=2)
    private BigDecimal size;

    @Column(nullable = false, precision=19, scale=2)
    private BigDecimal price;

    @Column(nullable = false)
    private int status;

    @Column(nullable = false)
    private LocalDateTime createDate;

    @Version
    private long version;
}
