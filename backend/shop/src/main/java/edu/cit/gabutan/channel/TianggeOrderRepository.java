package edu.cit.gabutan.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface TianggeOrderRepository
        extends JpaRepository<TianggeOrder, Long> {

    Optional<TianggeOrder> findByTianggeOrderId(String tianggeOrderId);
}