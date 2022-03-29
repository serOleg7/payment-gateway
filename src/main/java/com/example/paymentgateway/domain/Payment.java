package com.example.paymentgateway.domain;

import com.example.paymentgateway.enums.Currency;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
public class Payment {
    @Id
    private Integer invoice;
    private int amount;
    private Currency currency;
    @Embedded
    @Setter
    private Cardholder cardholder;
    @Embedded
    @Setter
    private Card card;
    private Date date;
    @Transient
    @Setter
    private Map<String, String> errors = null;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Payment payment = (Payment) o;
        return invoice != null && Objects.equals(invoice, payment.invoice);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
