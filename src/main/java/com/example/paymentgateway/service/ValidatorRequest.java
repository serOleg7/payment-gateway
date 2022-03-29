package com.example.paymentgateway.service;

import com.example.paymentgateway.dao.PaymentRepository;
import com.example.paymentgateway.dto.RequestDto;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@NoArgsConstructor
public class ValidatorRequest {
    @Autowired
    private PaymentRepository paymentRepository;


    public Map<String, String> getErrors(RequestDto requestDto) {
        Map<String, String> map = new LinkedHashMap<>();
        if (!isValidInvoice(requestDto))
            map.put("invoice", "Incorrect invoice");
        if (!isValidInteger(requestDto.getAmount()))
            map.put("amount", "Incorrect amount");
        if (requestDto.getCurrency() == null)
            map.put("currency", "Currency is required");
        if (requestDto.getCardholder() == null) {
            map.put("cardholder", "Cardholder not provided");
        } else {
            if (requestDto.getCardholder().getName() == null)
                map.put("name", "Name not provided");
            if (!isValidEmail(requestDto))
                map.put("email", "Invalid cardholder email format");
        }
        if (requestDto.getCard() == null)
            map.put("card", "Info of card not provided");
        else {
            if (requestDto.getCard().getPan() == null || requestDto.getCard().getPan().length() != 16 || !isValidLuhn(requestDto.getCard().getPan()))
                map.put("pan", "Incorrect pan or not provided");
            if (requestDto.getCard().getExpiry() == null || !isValidDate(requestDto.getCard().getExpiry()))
                map.put("expiry", "Payment card is expired or wrong");
            if (!isValidCvv(requestDto))
                map.put("cvv", "CVV incorrect or not provided");
        }
        return map.size() == 0 ? null : map;
    }

    private boolean isValidEmail(RequestDto requestDto) {
        return EmailValidator.getInstance().isValid(requestDto.getCardholder().getEmail());
    }

    private boolean isValidCvv(RequestDto requestDto) {
        String cvv = requestDto.getCard().getCvv();
        if (cvv == null)
            return false;
        Pattern p = Pattern.compile("^[0-9]{3,4}$");
        Matcher m = p.matcher(cvv);
        return m.matches();
    }

    private boolean isValidInteger(int number) {
        return number > 0;
    }

    private boolean isValidInvoice(RequestDto requestDto) {
        return isValidInteger(requestDto.getInvoice()) && !paymentRepository.existsById(requestDto.getInvoice());
    }

    private boolean isValidDate(String expiry) {
        try {
            YearMonth yearMonth = YearMonth.parse(expiry, DateTimeFormatter.ofPattern("MM/yy"));
            LocalDate expiryDate = yearMonth.atEndOfMonth();
            return !LocalDate.now().isAfter(expiryDate);
        }catch (Exception e){
            return false;
        }
    }

    private static boolean isValidLuhn(String value) {
        int sum = Character.getNumericValue(value.charAt(value.length() - 1));
        int parity = value.length() % 2;
        for (int i = value.length() - 2; i >= 0; i--) {
            int summand = Character.getNumericValue(value.charAt(i));
            if (i % 2 == parity) {
                int product = summand * 2;
                summand = (product > 9) ? (product - 9) : product;
            }
            sum += summand;
        }
        return (sum % 10) == 0;
    }

    public Map<String, String> getErrorsNotEmpty(RequestDto requestDto) {
        Map<String, String> map = getErrors(requestDto) != null ? getErrors(requestDto) : new HashMap<>();
        map.put("invoice", "Incorrect invoice");
        return map;
    }
}
