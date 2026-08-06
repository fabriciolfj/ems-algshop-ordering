package com.algaworks.algashop.ordering.core.ports.in.customer;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ForAddingLoyaltyPoints {
    @Transactional
    void addLoyaltyPoints(UUID rawCustomerId, String rawOrderId);
}
