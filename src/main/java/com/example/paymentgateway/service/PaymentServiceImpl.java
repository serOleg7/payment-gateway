package com.example.paymentgateway.service;

import com.example.paymentgateway.dao.PaymentRepository;
import com.example.paymentgateway.domain.Card;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.domain.Payment;
import com.example.paymentgateway.domain.ViewModel;
import com.example.paymentgateway.dto.NotApprovedDto;
import com.example.paymentgateway.dto.RequestDto;
import com.example.paymentgateway.dto.ResponseDto;
import com.example.paymentgateway.exceptions.PermissionDeniedException;
import com.example.paymentgateway.exceptions.TransactionNotFoundException;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final Set<Integer> uniqueInvoices = ConcurrentHashMap.newKeySet();
    private final PaymentRepository paymentRepository;
    private final ValidatorRequest validatorRequest;
    private final ModelMapper modelMapper;
    private final String salt;
    private final String pathFile;
    private TextEncryptor encryptor;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, ValidatorRequest validatorRequest, ModelMapper modelMapper, TextEncryptor encryptor, @Value("${salt.value}") String salt, @Value("${pathFile.value}") String pathFile) {
        this.paymentRepository = paymentRepository;
        this.validatorRequest = validatorRequest;
        this.modelMapper = modelMapper;
        this.encryptor = encryptor;
        this.salt = salt;
        this.pathFile = pathFile;
    }

    public void setEncryptor(String salt) {
        this.encryptor = Encryptors.text("1234", salt);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseDto> submitPayment(RequestDto requestDto) {
        setEncryptor(salt);
        boolean correctInvoiceId = uniqueInvoices.add(requestDto.getInvoice());
        Map<String, String> errors = correctInvoiceId ? validatorRequest.getErrors(requestDto) : validatorRequest.getErrorsNotEmpty(requestDto);
        Payment payment = encryptPayment(modelMapper.map(requestDto, Payment.class));
        ResponseEntity<ResponseDto> responseEntity;
        if (errors == null) {
            paymentRepository.save(payment);
            responseEntity = new ResponseEntity<>(new ResponseDto(true), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(new NotApprovedDto(false, errors), HttpStatus.BAD_REQUEST);
            payment.setErrors(errors);
        }
        saveToStorage(payment, pathFile);
        uniqueInvoices.remove(requestDto.getInvoice());
        return responseEntity;
    }


    @Override
    public ViewModel retrieveTransaction(int invoice, Cardholder cardholder) {
        Payment payment = paymentRepository.findById(invoice).orElseThrow(() -> new TransactionNotFoundException(invoice));
        setEncryptor(salt);
        decryptPayment(payment);
        if (!payment.getCardholder().toString().equals(cardholder.toString()))
            throw new PermissionDeniedException();
        return new ViewModel(payment);
    }

    private Payment encryptPayment(Payment payment) {
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


    private void decryptPayment(Payment payment) {
        //TODO FIX for decrypting from audit.json
        payment.getCardholder().setName(encryptor.decrypt(payment.getCardholder().getName()));
        payment.setCard(new Card(encryptor.decrypt(payment.getCard().getPan()), encryptor.decrypt(payment.getCard().getExpiry()), null));
    }

    private void saveToStorage(Payment payment, String pathFile) {
        //TODO Check for concurrency issues
        String jsonPayment = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(payment);
        StringBuilder res = getLastLine();
        if (res.length() == 0) {
            res.append("[\n").append(jsonPayment).append("]");
        } else {
            eraseLast();
            res.deleteCharAt(res.length() - 1);
            res.append(",\n").append(jsonPayment).append("]");
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(pathFile, true))) {
            out.print(res);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder getLastLine() {
        try (BufferedReader input = new BufferedReader(new FileReader(pathFile))) {
            String last = null, line;
            while ((line = input.readLine()) != null)
                last = line;
            return last == null ? new StringBuilder() : new StringBuilder(last);
        } catch (Exception e) {
            File file = new File(pathFile);
            return new StringBuilder();
        }
    }

    private void eraseLast() {
        try (RandomAccessFile f = new RandomAccessFile(pathFile, "rw")) {
            if (f.length() != 0) {
                long length = f.length() - 1;
                byte b;
                do {
                    length -= 1;
                    f.seek(length);
                    b = f.readByte();
                } while (b != 10);
                f.setLength(length + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
