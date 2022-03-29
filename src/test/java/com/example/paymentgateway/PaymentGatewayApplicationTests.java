package com.example.paymentgateway;

import com.example.paymentgateway.dao.PaymentRepository;
import com.example.paymentgateway.domain.Card;
import com.example.paymentgateway.domain.Cardholder;
import com.example.paymentgateway.domain.Payment;
import com.example.paymentgateway.domain.ViewModel;
import com.example.paymentgateway.dto.NotApprovedDto;
import com.example.paymentgateway.dto.RequestDto;
import com.example.paymentgateway.dto.ResponseDto;
import com.example.paymentgateway.enums.Currency;
import com.example.paymentgateway.exceptions.PermissionDeniedException;
import com.example.paymentgateway.exceptions.TransactionNotFoundException;
import com.example.paymentgateway.service.PaymentService;
import com.example.paymentgateway.service.PaymentServiceImpl;
import com.example.paymentgateway.service.TextEncryptorCustom;
import com.example.paymentgateway.service.ValidatorRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
@ActiveProfiles("test")
class PaymentGatewayApplicationTests {
    private static PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final ValidatorRequest validatorRequest;
    private final TextEncryptorCustom encryptor;
    private final ModelMapper modelMapper;
    @Value("${salt.value}") private String salt;
    @Value("${pathFile.value}") private String pathFile;
    RequestDto wrongRequestDto;

    @Autowired
    PaymentGatewayApplicationTests(PaymentRepository paymentRepository, ValidatorRequest validatorRequest, ModelMapper modelMapper, TextEncryptorCustom encryptor) {
        this.paymentRepository = paymentRepository;
        this.validatorRequest = validatorRequest;
        this.modelMapper = modelMapper;
        this.encryptor = encryptor;
    }

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, validatorRequest, encryptor, modelMapper, salt, pathFile);
        wrongRequestDto = new RequestDto(999, 11, Currency.EUR, new Cardholder("John", "seroleg7@gmailcom"), new Card("454671", "03/5", "123"));


    }

    @Test
    void checkCorrectSubmitTest() {
        long count = paymentRepository.count();
        RequestDto correctRequestDto = new RequestDto(1, 11, Currency.EUR, new Cardholder("John", "seroleg7@gmail.com"), new Card("4561261212345467", "03/25", "123"));
        ResponseEntity<ResponseDto> actual = paymentService.submitPayment(correctRequestDto);
        then(actual.getStatusCodeValue()).isEqualTo(200);

        then(paymentRepository.count()).isEqualTo(count+1);

        then(Objects.requireNonNull(actual.getBody()).toString()).isEqualTo(new ResponseDto(true).toString());

    }

    @Test
    void checkWrongSubmitTest() {
        long count = paymentRepository.count();
        ResponseEntity<ResponseDto> actual = paymentService.submitPayment(wrongRequestDto);
        then(actual.getStatusCodeValue()).isEqualTo(400);
        then(paymentRepository.count()).isEqualTo(count);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("email", "Invalid cardholder email format");
        map.put("pan", "Incorrect pan or not provided");
        map.put("expiry", "Payment card is expired or wrong");
        then(Objects.requireNonNull(actual.getBody()).toString()).isEqualTo(new NotApprovedDto(false, map).toString());
    }

    @Test
    void checkWrongSubmitTest_DuplicatedInvoice() {
        RequestDto correctRequestDto_2 = new RequestDto(2, 11, Currency.EUR, new Cardholder("Oleg", "seroleg7@gmail.com"), new Card("4561261212345467", "03/25", "123"));

        ResponseEntity<ResponseDto> actual = paymentService.submitPayment(correctRequestDto_2);
        then(actual.getStatusCodeValue()).isEqualTo(200);

        ResponseEntity<ResponseDto> actual_2 = paymentService.submitPayment(correctRequestDto_2);
        then(actual_2.getStatusCodeValue()).isEqualTo(400);

        then(Objects.requireNonNull(actual_2.getBody()).toString()).isEqualTo(new NotApprovedDto(false, Map.of("invoice", "Incorrect invoice")).toString());
    }

    @Test
    void checkRetrieveCorrectTransaction() {
        Cardholder cardholder = new Cardholder("John", "seroleg7@gmail.com");
        RequestDto correctRequestDto_3 = new RequestDto(3, 11, Currency.EUR, cardholder, new Card("4561261212345467", "03/25", "123"));

        paymentService.submitPayment(correctRequestDto_3);
        ViewModel actual = paymentService.retrieveTransaction(3, cardholder);

        ViewModel vm = new ViewModel(modelMapper.map(correctRequestDto_3, Payment.class));
        then(actual.toString()).isEqualTo(vm.toString());
    }


    @Test
    void checkRetrieveTransaction_DeniedExceptionExpected() {
        Cardholder cardholder = new Cardholder("Peter", "seroleg7@gmail.com");
        RequestDto correctRequestDto_4 = new RequestDto(4, 11, Currency.EUR, cardholder, new Card("4561261212345467", "03/25", "123"));
        paymentService.submitPayment(correctRequestDto_4);

        PermissionDeniedException thrown = Assertions.assertThrows(PermissionDeniedException.class, () ->
                paymentService.retrieveTransaction(4, new Cardholder("Olga", "seroleg7@gmail.com")));
        Assertions.assertEquals("Permission denied", thrown.getMessage());
    }

    @Test
    void checkRetrieveTransaction_NotFoundExpected() {
        int invoice = 777;
        TransactionNotFoundException thrown = Assertions.assertThrows(TransactionNotFoundException.class, () ->
                paymentService.retrieveTransaction(invoice, new Cardholder("Olga", "seroleg7@gmail.com")));
        Assertions.assertEquals("Transaction #"+invoice+" not found", thrown.getMessage());
    }


}
