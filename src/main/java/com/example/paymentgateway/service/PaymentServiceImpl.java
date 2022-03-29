package com.example.paymentgateway.service;

import com.example.paymentgateway.dao.PaymentRepository;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.domain.Payment;
import com.example.paymentgateway.domain.ViewModel;
import com.example.paymentgateway.dto.NotApprovedDto;
import com.example.paymentgateway.dto.RequestDto;
import com.example.paymentgateway.dto.ResponseDto;
import com.example.paymentgateway.exceptions.PermissionDeniedException;
import com.example.paymentgateway.exceptions.TransactionNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final Set<Integer> uniqueInvoices = ConcurrentHashMap.newKeySet();
    private final PaymentRepository paymentRepository;
    private final ValidatorRequest validatorRequest;
    private final TextEncryptorCustom encryptor;
    private final ModelMapper modelMapper;
    private final String salt;
    private final String pathFile;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, ValidatorRequest validatorRequest, TextEncryptorCustom encryptor, ModelMapper modelMapper,  @Value("${salt.value}") String salt, @Value("${pathFile.value}") String pathFile) {
        this.paymentRepository = paymentRepository;
        this.validatorRequest = validatorRequest;
        this.encryptor = encryptor;
        this.modelMapper = modelMapper;
        this.salt = salt;
        this.pathFile = pathFile;
    }


    @Override
    @Transactional
    public ResponseEntity<ResponseDto> submitPayment(RequestDto requestDto) {
        boolean correctInvoiceId = uniqueInvoices.add(requestDto.getInvoice());
        Map<String, String> errors = correctInvoiceId ? validatorRequest.getErrors(requestDto) : validatorRequest.getErrorsNotEmpty(requestDto);
        Payment payment = encryptor.encryptPayment(modelMapper.map(requestDto, Payment.class), salt);
        ResponseEntity<ResponseDto> responseEntity;
        if (errors == null) {
            paymentRepository.save(payment);
            responseEntity = new ResponseEntity<>(new ResponseDto(true), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(new NotApprovedDto(false, errors), HttpStatus.BAD_REQUEST);
            payment.setErrors(errors);
        }
        StorageUtils.saveToStorage(payment, pathFile);
        uniqueInvoices.remove(requestDto.getInvoice());
        return responseEntity;
    }


    @Override
    public ViewModel retrieveTransaction(int invoice, Cardholder cardholder) {
        Payment payment = paymentRepository.findById(invoice).orElseThrow(() -> new TransactionNotFoundException(invoice));
        encryptor.decryptPayment(payment, salt);
        if (!payment.getCardholder().toString().equals(cardholder.toString()))
            throw new PermissionDeniedException();
        return new ViewModel(payment);
    }



}
