package com.example.paymentgateway.domain;

import lombok.*;

import javax.persistence.Embeddable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Embeddable
public class Card {
    private String pan;
    private String expiry;
    private transient String cvv;
}
