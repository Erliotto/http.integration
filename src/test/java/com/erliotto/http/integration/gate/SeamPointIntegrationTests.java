package com.erliotto.http.integration.gate;

import com.erliotto.http.integration.Application;
import com.erliotto.http.integration.gate.internal.TestOnlyRestController;
import com.erliotto.http.integration.gate.internal.TestRestTemplateHttpResultProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                Application.class,
                TestOnlyRestController.class
        })
class SeamPointIntegrationTests {
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
    void call_whenGetString_thenReturnExpectedStatusAndData() throws JsonProcessingException {
        // arrange
        final class StringResponse extends DefaultHttpStatusHolder {
            public final String answer;

            public StringResponse(String rawResponse) {
                this.answer = rawResponse;
            }
        }

        final SeamPoint<StringResponse> seamPoint =
                new SeamPoint<StringResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String.class, rawResponse -> new StringResponse(rawResponse));

        final String url = String.format("http://localhost:%d/getString", port);

        // act
        final StringResponse stringResponse = seamPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(stringResponse)
                .isNotNull();

        assertThat(stringResponse.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(stringResponse.answer)
                .isEqualTo("just string value");
    }

    @Test
    void call_whenGetStringArray_thenReturnExpectedStatusAndData() throws JsonProcessingException {
        // arrange
        final class StringArray extends DefaultHttpStatusHolder {
            public final String[] data;

            public StringArray(final String[] data) {
                this.data = data;
            }
        }

        final SeamPoint<StringArray> seamPoint =
                new SeamPoint<StringArray>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String[].class, rawResponse -> new StringArray(rawResponse));

        final String url = String.format("http://localhost:%d/getStringArray", port);

        // act
        final StringArray stringArray = seamPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(stringArray)
                .isNotNull();

        assertThat(stringArray.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(stringArray.data)
                .isEqualTo(new String[]{"item 1", "item 2"});
    }

    @Test
    void call_whenGetJson_thenReturnExpectedStatusAndData() throws JsonProcessingException {
        // arrange
        final SeamPoint<TestOnlyRestController.ReturnTypes.Json> seamPoint =
                new SeamPoint<TestOnlyRestController.ReturnTypes.Json>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, TestOnlyRestController.ReturnTypes.Json.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        final TestOnlyRestController.ReturnTypes.Json json = seamPoint.call(HttpMethod.GET, url, null, null);

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
    void call_whenGetJsonUnexpectedFieldsAndDefaultObjectMapper_thenThrowJsonProcessingException() {
        // arrange
        final ObjectMapper mapper = new ObjectMapper();

        final SeamPoint<JsonUnexpected> seamPoint =
                new SeamPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        assertThatThrownBy(() -> seamPoint.call(HttpMethod.GET, url, null, null))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void call_whenGetJsonUnexpectedFieldsAndSpecialObjectMapper_thenReturnExpectedStatusAndData() throws JsonProcessingException {
        // arrange
        final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final SeamPoint<JsonUnexpected> seamPoint =
                new SeamPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        final String url = String.format("http://localhost:%d/getJson", port);

        // act
        final JsonUnexpected json = seamPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(json)
                .isNotNull();

        assertThat(json.getHttpStatus())
                .isEqualTo(HttpStatus.OK);

        assertThat(json.name)
                .isEqualTo("json name");
    }

    @Test
    void call_whenCallUnknownUrl_thenReturnNull() throws JsonProcessingException {
        final class StringResponse extends DefaultHttpStatusHolder {
            public final String answer;

            public StringResponse(String rawResponse) {
                this.answer = rawResponse;
            }
        }
        // arrange
        final SeamPoint<StringResponse> seamPoint =
                new SeamPoint<StringResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, StringResponse.class);

        final String url = String.format("http://localhost:%d/unknownUrl", port);

        // act
        final StringResponse stringResponse = seamPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(stringResponse)
                .isNull();
    }

    @Test
    void call_whenCallUnknownUrlAndRegisterDefault_thenReturn404Status() throws JsonProcessingException {
        abstract class BaseResponse extends DefaultHttpStatusHolder {
            public final String answer;

            public BaseResponse(String rawResponse) {
                this.answer = rawResponse;
            }
        }

        final class StringResponse extends BaseResponse {
            public StringResponse(String rawResponse) {
                super(rawResponse);
            }
        }

        final class UnknownResponse extends BaseResponse {
            public UnknownResponse(String rawResponse) {
                super(rawResponse);
            }
        }

        // arrange
        final SeamPoint<BaseResponse> seamPoint =
                new SeamPoint<BaseResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, StringResponse.class)
                        .registerDefault(String.class, rawResponse -> new UnknownResponse(rawResponse));

        final String url = String.format("http://localhost:%d/unknownUrl", port);

        // act
        final BaseResponse baseResponse = seamPoint.call(HttpMethod.GET, url, null, null);

        // assert
        assertThat(baseResponse)
                .isNotNull();

        assertThat(baseResponse.getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(baseResponse)
                .isInstanceOf(UnknownResponse.class);
    }

}
