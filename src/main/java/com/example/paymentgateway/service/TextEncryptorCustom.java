package com.example.paymentgateway.service;

import com.example.paymentgateway.domain.Card;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.domain.Payment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class TextEncryptorCustom {
    private TextEncryptor encryptor;

    public Payment encryptPayment(Payment payment, String salt) {
        encryptor = Encryptors.text("1234", salt);
        if (payment.getCardholder() != null && payment.getCardholder().getName() != null) {
            Cardholder cardholder = new Cardholder(encryptor.encrypt(payment.getCardholder().getName()), payment.getCardholder().getEmail());
            payment.setCardholder(cardholder);
        }
        Card card = new Card("", "", null);
        if (payment.getCard() != null) {
            if (payment.getCard().getPan() != null)
                card.setPan(encryptor.encrypt(payment.getCard().getPan()));
            if (payment.getCard().getExpiry() != null)
                card.setExpiry(encryptor.encrypt(payment.getCard().getExpiry()));
        }
        payment.setCard(card);
        return payment;
    }


    public void decryptPayment(Payment payment, String salt) {
        encryptor = Encryptors.text("1234", salt);

        //TODO FIX for decrypting from audit.json
        payment.getCardholder().setName(encryptor.decrypt(payment.getCardholder().getName()));
        payment.setCard(new Card(encryptor.decrypt(payment.getCard().getPan()), encryptor.decrypt(payment.getCard().getExpiry()), null));
    }
}