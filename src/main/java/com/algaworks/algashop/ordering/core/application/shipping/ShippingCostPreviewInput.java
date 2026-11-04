package com.algaworks.algashop.ordering.core.application.shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ShippingCostPreviewInput {

    @NotBlank
    @Size(min = 5, max = 5)
    private String zipCode;
}
