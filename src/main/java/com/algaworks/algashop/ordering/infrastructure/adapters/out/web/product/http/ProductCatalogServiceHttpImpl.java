package com.algaworks.algashop.ordering.infrastructure.adapters.out.web.product.http;

import com.algaworks.algashop.ordering.core.domain.model.commons.Money;
import com.algaworks.algashop.ordering.core.domain.model.product.Product;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductCatalogService;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductId;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductCatalogServiceHttpImpl implements ProductCatalogService {

    private final ResilientProductCatalogAPIClient resilientProductCatalogAPIClient;

    @Override
    public Optional<Product> ofId(ProductId productId) {
        return resilientProductCatalogAPIClient.ofId(productId.value())
                        .map(productResponse ->
                            Product.builder()
                                    .id(new ProductId(productResponse.getId()))
                                    .name(new ProductName(productResponse.getName()))
                                    .inStock(productResponse.getInStock())
                                    .price(new Money(productResponse.getSalePrice()))
                                    .build());
    }
}
