package edu.cit.gabutan.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierOrderRepository
        extends JpaRepository<SupplierOrder, Long> {

    Optional<SupplierOrder> findByBuyerRef(String buyerRef);

    Optional<SupplierOrder> findByRequestId(String requestId);
}