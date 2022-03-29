package com.example.paymentgateway.service;

import com.example.paymentgateway.domain.ViewModel;
import com.example.paymentgateway.dto.RequestDto;
import com.example.paymentgateway.dto.ResponseDto;
import com.example.paymentgateway.domain.Cardholder;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<ResponseDto> submitPayment(RequestDto requestDto);

    ViewModel retrieveTransaction(int invoice, Cardholder cardholder);
}
