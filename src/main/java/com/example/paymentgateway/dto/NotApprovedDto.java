package com.example.paymentgateway.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;


@Getter
@ToString
public class NotApprovedDto extends ResponseDto{
    private final Map<String, String> errors;

    public NotApprovedDto(Boolean approved, Map<String, String> errors) {
        super(approved);
        this.errors = errors;
    }
}
