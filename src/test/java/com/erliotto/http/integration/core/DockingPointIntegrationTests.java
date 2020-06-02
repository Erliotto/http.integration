package com.erliotto.http.integration.core;

import com.erliotto.http.integration.Application;
import com.erliotto.http.integration.core.internal.TestOnlyRestController;
import com.erliotto.http.integration.core.internal.TestRestTemplateHttpResultProvider;
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
class DockingPointIntegrationTests {
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

    private String createUrl(String path) {
        return String.format("http://localhost:%d/%s", port, path);
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

        final DockingPoint<StringResponse> dockingPoint =
                new DockingPoint<StringResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String.class, rawResponse -> new StringResponse(rawResponse));

        // act
        final StringResponse stringResponse = dockingPoint.call(HttpMethod.GET, createUrl("getString"), null, null);

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

        final DockingPoint<StringArray> dockingPoint =
                new DockingPoint<StringArray>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, String[].class, rawResponse -> new StringArray(rawResponse));

        // act
        final StringArray stringArray = dockingPoint.call(HttpMethod.GET, createUrl("getStringArray"), null, null);

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
        final DockingPoint<TestOnlyRestController.ReturnTypes.Json> dockingPoint =
                new DockingPoint<TestOnlyRestController.ReturnTypes.Json>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, TestOnlyRestController.ReturnTypes.Json.class);

        // act
        final TestOnlyRestController.ReturnTypes.Json json = dockingPoint.call(HttpMethod.GET, createUrl("getJson"), null, null);

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

        final DockingPoint<JsonUnexpected> dockingPoint =
                new DockingPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        // act
        assertThatThrownBy(() -> dockingPoint.call(HttpMethod.GET, createUrl("getJson"), null, null))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void call_whenGetJsonUnexpectedFieldsAndSpecialObjectMapper_thenReturnExpectedStatusAndData() throws JsonProcessingException {
        // arrange
        final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final DockingPoint<JsonUnexpected> dockingPoint =
                new DockingPoint<JsonUnexpected>(createHttpResultProvider(), mapper)
                        .register(HttpStatus.OK, JsonUnexpected.class);

        // act
        final JsonUnexpected json = dockingPoint.call(HttpMethod.GET, createUrl("getJson"), null, null);

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
        final DockingPoint<StringResponse> dockingPoint =
                new DockingPoint<StringResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, StringResponse.class);

        // act
        final StringResponse stringResponse = dockingPoint.call(HttpMethod.GET, createUrl("unknownUrl"), null, null);

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
        final DockingPoint<BaseResponse> dockingPoint =
                new DockingPoint<BaseResponse>(createHttpResultProvider(), new ObjectMapper())
                        .register(HttpStatus.OK, StringResponse.class)
                        .registerDefault(String.class, rawResponse -> new UnknownResponse(rawResponse));

        // act
        final BaseResponse baseResponse = dockingPoint.call(HttpMethod.GET, createUrl("unknownUrl"), null, null);

        // assert
        assertThat(baseResponse)
                .isNotNull();

        assertThat(baseResponse.getHttpStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(baseResponse)
                .isInstanceOf(UnknownResponse.class);
    }

}
