package com.UrbanVogue.admin.Inventory.repository;

import com.UrbanVogue.admin.Inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Inventory findByProductId(Long productId);

}