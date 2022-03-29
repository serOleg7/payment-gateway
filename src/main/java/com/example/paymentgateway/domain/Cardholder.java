package com.example.paymentgateway.domain;

import lombok.*;

import javax.persistence.Embeddable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Embeddable
public class Cardholder {
    @Setter
    private String name;
    private String email;
}
