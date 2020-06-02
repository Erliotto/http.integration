package com.erliotto.http.integration.component;

import com.erliotto.http.integration.core.DockingPoint;
import com.erliotto.http.integration.core.HttpResultProvider;
import com.erliotto.http.integration.core.HttpStatusHolder;
import com.erliotto.http.integration.core.WebClientHttpResultProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DockingPointComponent {
    @Bean
    <Response extends HttpStatusHolder> DockingPoint<Response> createDockingPoint(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        final HttpResultProvider httpResultProvider = new WebClientHttpResultProvider(webClientBuilder);
        final DockingPoint<Response> dockingPoint = new DockingPoint<>(httpResultProvider, objectMapper);
        return dockingPoint;
    }

    @Bean
    ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
