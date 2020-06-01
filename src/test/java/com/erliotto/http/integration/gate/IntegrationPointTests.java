package com.erliotto.http.integration.gate;

import com.erliotto.http.integration.Application;
import com.erliotto.http.integration.gate.internal.TestOnlyRestController;
import com.erliotto.http.integration.gate.internal.TestRestTemplateHttpResultProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                Application.class,
                TestOnlyRestController.class
        })
class IntegrationPointTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpResultProvider createHttpResultProvider() {
        return new TestRestTemplateHttpResultProvider(restTemplate);
    }

    private static final class JsonUnexpected extends DefaultHttpStatusHolder {
        @JsonProperty("address")
        public final String address;

        @JsonProperty("name")
        public final String name;

        @JsonProperty("zip")
        public final String zip;

        @JsonCreator
        public JsonUnexpected(@JsonProperty("address") String address,
                              @JsonProperty("name") String name,
                              @JsonProperty("zip") String zip
        ) {
            this.address = address;
            this.name = name;
            this.zip = zip;
        }
    }


    @Test
    void call_whenGetString_thenReturnExpectedStatusAndData() {
        // arrange
        final class StringResponse extends DefaultHttpStatusHolder {
            public final String answer;

            public StringResponse(String rawResponse) {
                this.answer = rawResponse;
            }
        }

        final IntegrationPoint<StringResponse> integrationPoint =
                new IntegrationPoint<StringResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String.class, rawResponse -> new StringResponse(rawResponse));

        final String url = String.format("http://localhost:%d/getString", port);

        // act
        final StringResponse stringResponse = integrationPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(stringResponse)
                .isNotNull();

        assertThat(stringResponse.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(stringResponse.answer)
                .isEqualTo("just string value");
    }

    @Test
    void call_whenGetStringArray_thenReturnExpectedStatusAndData() {
        // arrange
        final class StringArray extends DefaultHttpStatusHolder {
            public final String[] data;

            public StringArray(final String[] data) {
                this.data = data;
            }
        }

        final IntegrationPoint<StringArray> integrationPoint =
                new IntegrationPoint<StringArray>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String[].class, rawResponse -> new StringArray(rawResponse));

        final String url = String.format("http://localhost:%d/getStringArray", port);

        // act
        final StringArray stringArray = integrationPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(stringArray)
                .isNotNull();

        assertThat(stringArray.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(stringArray.data)
                .isEqualTo(new String[]{"item 1", "item 2"});
    }

    @Test
    void call_whenGetJson_thenReturnExpectedStatusAndData() {
        // arrange
        final IntegrationPoint<TestOnlyRestController.ReturnTypes.Json> integrationPoint =
                new IntegrationPoint<TestOnlyRestController.ReturnTypes.Json>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, TestOnlyRestController.ReturnTypes.Json.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        final TestOnlyRestController.ReturnTypes.Json json = integrationPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(json)
                .isNotNull();

        assertThat(json.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(json.id)
                .isEqualTo(102);

        assertThat(json.name)
                .isEqualTo("json name");
    }

    @Test
    void call_whenGetJsonUnexpectedFieldsAndDefaultObjectMapper_thenReturnNull() {
        // arrange
        final ObjectMapper mapper = new ObjectMapper();

        final IntegrationPoint<JsonUnexpected> integrationPoint =
                new IntegrationPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        final JsonUnexpected json = integrationPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(json)
                .isNull();
    }

    @Test
    void call_whenGetJsonUnexpectedFieldsAndSpecialObjectMapper_thenReturnExpectedStatusAndData() {
        // arrange
        final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final IntegrationPoint<JsonUnexpected> integrationPoint =
                new IntegrationPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        final JsonUnexpected json = integrationPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(json)
                .isNotNull();

        assertThat(json.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(json.name)
                .isEqualTo("json name");
    }
}
