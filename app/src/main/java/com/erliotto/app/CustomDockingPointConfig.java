package com.erliotto.app;

import com.erliotto.http.integration.core.HttpResultProvider;
import com.erliotto.http.integration.core.RestTemplateHttpResultProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//
// uncomment for custom configuration
//
// @Configuration
public class CustomDockingPointConfig {

    @Bean
    HttpResultProvider createHttpResultProvider(RestTemplate restTemplate) {
        return new RestTemplateHttpResultProvider(restTemplate);
    }

    @Bean
    RestTemplate createRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}
