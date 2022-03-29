package com.example.paymentgateway.dto;

import com.example.paymentgateway.domain.Card;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class RequestDto {
    private int invoice;
    private int amount;
    private Currency currency;
    private Cardholder cardholder;
    private Card card;
    private final Date date = new Date();


}
