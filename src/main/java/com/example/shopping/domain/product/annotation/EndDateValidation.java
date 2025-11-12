package com.example.shopping.domain.product.annotation;


import com.example.shopping.domain.product.dto.request.AddEventProductRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndDateValidation implements ConstraintValidator<EndDate, AddEventProductRequest> {

    @Override
    public boolean isValid(AddEventProductRequest request,
        ConstraintValidatorContext constraintValidatorContext) {
        if (request.getStart() == null || request.getEnd() == null) {
            return true; // null이면 검증하지 않음
        }
        return request.getEnd().isAfter(request.getStart());
    }
}
