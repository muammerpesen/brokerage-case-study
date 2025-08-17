package com.stock.brokerage.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "asset")
@Getter
@Setter
public class Asset {

    public Asset() {
    }

    public Asset(int id, int customerId, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.id = id;
        this.customerId = customerId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private int customerId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal size;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal usableSize;

    @Version
    private long version;
}