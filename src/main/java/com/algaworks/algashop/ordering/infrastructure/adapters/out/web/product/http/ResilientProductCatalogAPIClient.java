package com.algaworks.algashop.ordering.infrastructure.adapters.out.web.product.http;

import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResilientProductCatalogAPIClient {

    private final ProductCatalogAPIClient productCatalogAPIClient;

    @Cacheable(cacheNames = "algashop:prodct-catalog-api:v1", key = "#productId")
    @ConcurrencyLimit(2)
    @Retryable(
            maxRetries = 3,
            delayString = "3s",
            multiplier = 2,
            includes = {
                    GatewayTimeoutException.class,
                    BadGatewayException.class
            }
    )
    public Optional<ProductResponse> ofId(UUID productId) {
        log.info("loading product {}", productId);
        try {
            return ofNullable(productCatalogAPIClient.getById(productId));
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException("Product Catalog API Timeout", e);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new GatewayTimeoutException("product catalog timeout", e);
            }

            throw new BadGatewayException("Product Catalog API Bad Gateway", e);
        }
    }
}
