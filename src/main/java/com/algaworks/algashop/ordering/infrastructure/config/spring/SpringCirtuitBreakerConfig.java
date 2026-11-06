package com.algaworks.algashop.ordering.infrastructure.config.spring;

import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@Configuration
public class SpringCirtuitBreakerConfig {

    public static final String PRODUCT_CATALOG_CB_ID = "productCatalogCB";
    public static final String RAPIDEX_CB_ID =  "rapidexAPICB";

    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> defaultCustomizer() {
        final RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .multiplier(2)
                .delay(Duration.ofSeconds(3))
                .includes(GatewayTimeoutException.class, BadGatewayException.ServerErrorException.class, BadGatewayException.class)
                .build();

        return factory -> {
            factory.configure(
                    buider -> buider
                            .openTimeout(Duration.ofSeconds(10))
                            .resetTimeout(Duration.ofSeconds(25))
                            .retryPolicy(retryPolicy)
                            .build(), PRODUCT_CATALOG_CB_ID
            );

            factory.configure(
                    buider -> buider
                            .openTimeout(Duration.ofSeconds(10))
                            .resetTimeout(Duration.ofSeconds(25))
                            .retryPolicy(retryPolicy)
                            .build(), RAPIDEX_CB_ID
            );
        };
    }
}
