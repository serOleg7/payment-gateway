package com.example.paymentgateway.configuration;

import com.example.paymentgateway.service.TextEncryptorCustom;
import com.example.paymentgateway.service.ValidatorRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public TextEncryptorCustom getTextEncryptor(){
        return new TextEncryptorCustom();
    }


    @Bean
    public ValidatorRequest getValidatorRequest(){
        return new ValidatorRequest();
    }

}
