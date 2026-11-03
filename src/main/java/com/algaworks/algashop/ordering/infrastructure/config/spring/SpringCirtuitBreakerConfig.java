package com.algaworks.algashop.ordering.infrastructure.config.spring;

import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

@Configuration
public class SpringCirtuitBreakerConfig {

    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> defaultCustomizer() {
        final RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(3)
                .multiplier(2)
                .delay(Duration.ofSeconds(3))
                .includes(GatewayTimeoutException.class, BadGatewayException.ServerErrorException.class)
                .build();

        return factory -> {
            factory.configure(
                    buider -> buider
                            .openTimeout(Duration.ofSeconds(10))
                            .resetTimeout(Duration.ofSeconds(25))
                            .retryPolicy(retryPolicy)
                            .build(), "productCatalogCB"
            );

            factory.configure(
                    buider -> buider
                            .openTimeout(Duration.ofSeconds(10))
                            .resetTimeout(Duration.ofSeconds(25))
                            .retryPolicy(retryPolicy)
                            .build(), "rapidexAPICB"
            );
        };
    }
}
