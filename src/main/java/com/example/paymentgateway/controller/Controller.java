package com.example.paymentgateway.controller;

import com.example.paymentgateway.domain.ViewModel;
import com.example.paymentgateway.dto.RequestDto;
import com.example.paymentgateway.dto.ResponseDto;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class Controller {
    final PaymentService paymentService;

    @Autowired
    public Controller(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> submitPayment(@RequestBody RequestDto requestDto) {
        return paymentService.submitPayment(requestDto);
    }

    @GetMapping("/{invoice}")
    public ViewModel get(@PathVariable int invoice, @RequestBody Cardholder cardholder){
        return paymentService.retrieveTransaction(invoice, cardholder);
    }
}
