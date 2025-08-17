package com.stock.brokerage.repositories;

import com.stock.brokerage.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Integer> {
    List<Asset> findByCustomerId(int customerId);
    List<Asset> findByCustomerIdAndAssetName(int customerId, String assetName);
}