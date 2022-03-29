package com.example.paymentgateway.dto;

import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class ResponseDto {
    private final Boolean approved;

    public ResponseDto(Boolean approved) {
        this.approved = approved;
    }


}
