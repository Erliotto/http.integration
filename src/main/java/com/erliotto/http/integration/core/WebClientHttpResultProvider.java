package com.erliotto.http.integration.core;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public final class WebClientHttpResultProvider implements HttpResultProvider {
    private final WebClient.Builder webClientBuilder;

    public WebClientHttpResultProvider(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Result call(HttpMethod httpMethod, String url, HttpHeaders httpHeaders, Object payload) {
        try {
            final WebClient.RequestBodyUriSpec method = webClientBuilder
                    .baseUrl(url)
                    .build()
                    .method(httpMethod);

            if (payload != null) {
                method.bodyValue(payload);
            }

            if (httpHeaders != null) {
                method.headers(h -> h.addAll(httpHeaders));
            }

            final Mono<ClientResponse> clientResponseMono = method.exchange();
            final ClientResponse clientResponse = clientResponseMono.block();
            final String body = clientResponse.bodyToMono(String.class).block();

            return new HttpResultProvider.Result(clientResponse.statusCode(), body);
        } catch (WebClientResponseException e) {
            return new HttpResultProvider.Result(e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
