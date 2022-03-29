package com.example.paymentgateway.domain;

import com.example.paymentgateway.enums.Currency;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ViewModel {
    private final Integer invoice;
    private final int amount;
    private final Currency currency;
    private final Cardholder cardholder;
    private final Card card;

    public ViewModel(Payment payment) {
        this.invoice = payment.getInvoice();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.cardholder = payment.getCardholder();
        this.card = payment.getCard();
        maskPayment();
    }


    private void maskPayment() {
        cardholder.setName("*************");
        card.setPan(maskPan(card.getPan()));
        card.setExpiry("****");
        card.setCvv("excluded");
    }

    private String maskPan(String pan) {
        return "************" + pan.substring(pan.length() - 4);
    }
}
