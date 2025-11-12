package com.example.shopping.domain.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventProduct {

    private Long productId;
    private String productName;
    private Integer eventPrice;
    private Integer eventStock;
    private LocalDateTime start;
    private LocalDateTime end;
}
