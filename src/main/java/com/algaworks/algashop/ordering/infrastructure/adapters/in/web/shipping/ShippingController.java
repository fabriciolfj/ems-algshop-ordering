package com.algaworks.algashop.ordering.infrastructure.adapters.in.web.shipping;

import com.algaworks.algashop.ordering.core.application.shipping.ShippingApplicationService;
import com.algaworks.algashop.ordering.core.application.shipping.ShippingCostPreviewInput;
import com.algaworks.algashop.ordering.core.application.shipping.ShippingCostPreviewOutput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-cost-previews")
public class ShippingController {

    private final ShippingApplicationService shippingApplicationService;

    @PostMapping
    public ShippingCostPreviewOutput previewShippingCost(@Valid @RequestBody final ShippingCostPreviewInput input) {
        return shippingApplicationService.previewOutput(input);
    }
}
