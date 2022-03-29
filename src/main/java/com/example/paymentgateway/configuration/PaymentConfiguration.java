package com.example.paymentgateway.configuration;

import com.example.paymentgateway.service.ValidatorRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class PaymentConfiguration {

    @Bean
    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldAccessLevel(AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true);
        return modelMapper;
    }

    @Bean
    public TextEncryptor getTextEncryptor(){
        return new TextEncryptor() {
            @Override
            public String encrypt(String text) { return text; }

            @Override
            public String decrypt(String encryptedText) { return encryptedText; }
        };
    }


    @Bean
    public ValidatorRequest getValidatorRequest(){
        return new ValidatorRequest();
    }

}
