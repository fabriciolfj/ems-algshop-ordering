package com.algaworks.algashop.ordering.infrastructure.adapters.out.web.product.http;

import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.UUID;

import static com.algaworks.algashop.ordering.infrastructure.config.spring.SpringCirtuitBreakerConfig.PRODUCT_CATALOG_CB_ID;
import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class ResilientProductCatalogAPIClient {

    private static final String MESSAGE_DEFAULT = "product catalog bad gateway";

    private final ProductCatalogAPIClient productCatalogAPIClient;
    private final CircuitBreaker productCatalogCB;

    public ResilientProductCatalogAPIClient(ProductCatalogAPIClient productCatalogAPIClient,
                                            CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory) {
        this.productCatalogAPIClient = productCatalogAPIClient;
        this.productCatalogCB = circuitBreakerFactory.create(PRODUCT_CATALOG_CB_ID);
    }

    @Cacheable(cacheNames = "algashop:prodct-catalog-api:v1", key = "#productId", unless = "#result == null")
    @ConcurrencyLimit(2)
    public Optional<ProductResponse> ofId(UUID productId) {
        log.info("loading product {}", productId);
        try {
            return productCatalogCB.run(() -> loadProduct(productId));
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    private RuntimeException unwrapException(NoFallbackAvailableException e) {
        if (e.getCause() instanceof RetryException re) {
            if (re.getCause() instanceof GatewayTimeoutException gte) {
                throw gte;
            }

            if (re.getCause() instanceof BadGatewayException bde) {
                throw bde;
            }
        }

        throw e;
    }

    private @NonNull Optional<ProductResponse> loadProduct(UUID productId) {
        try {
            return ofNullable(productCatalogAPIClient.getById(productId));
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException("Product Catalog API Timeout", e);
        } catch (HttpClientErrorException e) {
            if (!(e instanceof HttpClientErrorException.NotFound)) {
                log.error("client http error when loading product {}", productId, e);
            }

            return Optional.empty();
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private RuntimeException translateException(RestClientException e) {
        if (e.getCause() instanceof SocketTimeoutException || e instanceof ResourceAccessException) {
            return new GatewayTimeoutException("product catalog api timeout", e);
        }

        if (e instanceof HttpClientErrorException) {
            return new BadGatewayException.ClientErrorException(MESSAGE_DEFAULT, e);
        }

        if (e instanceof HttpServerErrorException) {
            return new BadGatewayException.ServerErrorException(MESSAGE_DEFAULT, e);
        }

        return new BadGatewayException(MESSAGE_DEFAULT, e);
    }
}
