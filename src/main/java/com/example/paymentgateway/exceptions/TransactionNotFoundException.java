package com.example.paymentgateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(int invoice) {
        super("Transaction #"+invoice+" not found");
    }
}
