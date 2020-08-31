package com.erliotto.http.integration.component;

import com.erliotto.http.integration.core.HttpResultProvider;
import com.erliotto.http.integration.core.WebClientHttpResultProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties
public class DockingPointConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    HttpResultProvider createHttpResultProvider(WebClient.Builder webClientBuilder) {
        return new WebClientHttpResultProvider(webClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
