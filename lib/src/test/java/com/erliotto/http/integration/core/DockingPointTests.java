package com.erliotto.http.integration.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class DockingPointTests {
    @MockBean
    RestTemplate mockRestTemplate;

    ObjectMapper objectMapper;

    @Mock
    ObjectMapper mockObjectMapper;

    private final static class ReturnTypes {
        public static final class OkResponse extends DefaultHttpStatusHolder {
            @JsonProperty("Id")
            public final String id;

            @JsonCreator
            public OkResponse(@JsonProperty("Id") String id) {
                this.id = id;
            }
        }

        public static final class UnexpectedResponse extends DefaultHttpStatusHolder {
            @JsonProperty("Name")
            public final String name;

            @JsonCreator
            public UnexpectedResponse(@JsonProperty("Name") String name) {
                this.name = name;
            }
        }
    }

    private final static class ResponseFromInt extends DefaultHttpStatusHolder {
        public ResponseFromInt(int data) {
        }
    }

    @BeforeEach
    void beforeEach() {
        objectMapper = new ObjectMapper();
    }

    private HttpStatusHolder act(DockingPoint dockingPoint, Consumer<OngoingStubbing<ResponseEntity>> whenConsumer) throws JsonProcessingException {
        final String url = "some url";
        final HttpHeaders httpHeaders = null;
        final Object payload = null;

        // arrange http response..
        if (whenConsumer != null) {
            OngoingStubbing<ResponseEntity> when = Mockito
                    // String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables
                    .when(mockRestTemplate.exchange(
                            Mockito.any(String.class),
                            Mockito.any(HttpMethod.class),
                            Mockito.any(HttpEntity.class),
                            Mockito.any(Class.class),
                            Mockito.any(Object[].class)
                    ));

            // arrange
            whenConsumer.accept(when);
        }

        // run interaction..
        return dockingPoint.call(HttpMethod.GET, url, httpHeaders, payload);
    }

    private HttpStatusHolder actWithResponse(DockingPoint dockingPoint, ResponseEntity<String> responseEntity) throws JsonProcessingException {
        return act(dockingPoint, when -> when.thenReturn(responseEntity));
    }

    private ResponseEntity<String> createResponseEntity(HttpStatus status, Object stubResponse) {
        final String responseStub;
        try {
            responseStub = objectMapper.writeValueAsString(stubResponse);
            return new ResponseEntity(responseStub, status);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private HttpResultProvider createHttpResultProvider() {
        return new RestTemplateHttpResultProvider(mockRestTemplate);
    }

    @Test
    void register_onDuplicateKey_shouldThrowIllegalArgumentException() {
        // arrange
        final HttpStatus httpStatus = HttpStatus.OK;
        final ReturnTypes.OkResponse expectedResponse = new ReturnTypes.OkResponse("id value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .register(httpStatus, expectedResponse.getClass());

        // act
        assertThatThrownBy(() -> dockingPoint.register(httpStatus, expectedResponse.getClass()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already have status");
    }

    @Test
    void call_whenHttpResultIsNull_shouldReturnNull() throws JsonProcessingException {
        // arrange
        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper);

        // act
        final HttpStatusHolder actual = act(dockingPoint, null);

        // assert
        assertThat(actual)
                .isNull();
    }

    @Test
    void call_whenRegisterAndExpectedResponseStatus_shouldReturnExpectedData() throws JsonProcessingException {
        // arrange
        final HttpStatus expectedResponseStatus = HttpStatus.OK;
        final ReturnTypes.OkResponse expectedResponse = new ReturnTypes.OkResponse("id value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .register(expectedResponseStatus, expectedResponse.getClass());

        // act
        final HttpStatusHolder actual = act(dockingPoint, when -> when.thenReturn(createResponseEntity(expectedResponseStatus, expectedResponse)));

        // assert
        assertThat(actual)
                .isNotNull();

        assertThat(actual.getHttpStatus())
                .isEqualTo(expectedResponseStatus);

        assertThat(actual)
                .isInstanceOf(ReturnTypes.OkResponse.class);

        assertThat(((ReturnTypes.OkResponse) actual).id)
                .isEqualTo(expectedResponse.id);
    }

    @Test
    void call_whenRegisterAndNotExpectedResponseStatus_shouldReturnNul() throws JsonProcessingException {
        // arrange
        final HttpStatus expectedResponseStatus = HttpStatus.OK;
        final ReturnTypes.OkResponse expectedResponse = new ReturnTypes.OkResponse("id value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .register(expectedResponseStatus, expectedResponse.getClass());

        // act
        final HttpStatusHolder actual = act(dockingPoint,
                when -> when.thenReturn(createResponseEntity(HttpStatus.ALREADY_REPORTED, expectedResponse)));

        // assert
        assertThat(actual)
                .isNull();
    }

    @Test
    void call_whenRegisterDefaultAndNotExpectedResponseStatus_shouldReturnExpectedData() throws JsonProcessingException {
        // arrange
        final HttpStatus defaultStatus = HttpStatus.ALREADY_REPORTED;
        final ReturnTypes.UnexpectedResponse defaultResponse = new ReturnTypes.UnexpectedResponse("value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .registerDefault(defaultResponse.getClass());

        // act
        final HttpStatusHolder actual = act(dockingPoint,
                when -> when.thenReturn(createResponseEntity(defaultStatus, defaultResponse)));

        // assert
        assertThat(actual)
                .isNotNull();

        assertThat(actual.getHttpStatus())
                .isEqualTo(defaultStatus);

        assertThat(actual)
                .isInstanceOf(ReturnTypes.UnexpectedResponse.class);

        assertThat(((ReturnTypes.UnexpectedResponse) actual).name)
                .isEqualTo(defaultResponse.name);
    }

    @Test
    void call_whenRegisterAndNotExpectedResponseDataFormat_shouldThrowJsonProcessingException() {
        // arrange
        final HttpStatus expectedResponseStatus = HttpStatus.OK;
        final ReturnTypes.OkResponse expectedResponse = new ReturnTypes.OkResponse("id value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .register(expectedResponseStatus, expectedResponse.getClass());

        final ReturnTypes.UnexpectedResponse unexpectedResponse = new ReturnTypes.UnexpectedResponse("value");

        // act
        assertThatThrownBy(() ->
                act(dockingPoint, when -> when.thenReturn(createResponseEntity(expectedResponseStatus, unexpectedResponse))))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void call_whenRegisterAndRegisterDefaultAndArriveExpectedStatus_shouldReturnExpectedData() throws JsonProcessingException {
        // arrange
        final HttpStatus expectedResponseStatus = HttpStatus.OK;
        final ReturnTypes.OkResponse expectedResponse = new ReturnTypes.OkResponse("id value");

        final DockingPoint dockingPoint = new DockingPoint(createHttpResultProvider(), objectMapper)
                .register(expectedResponseStatus, expectedResponse.getClass())
                .registerDefault(ReturnTypes.UnexpectedResponse.class);

        // act
        final HttpStatusHolder actual = act(dockingPoint,
                when -> when.thenReturn(createResponseEntity(expectedResponseStatus, expectedResponse)));

        // assert
        assertThat(actual)
                .isNotNull();

        assertThat(actual.getHttpStatus())
                .isEqualTo(expectedResponseStatus);

        assertThat(actual)
                .isInstanceOf(ReturnTypes.OkResponse.class);

        assertThat(((ReturnTypes.OkResponse) actual).id)
                .isEqualTo(expectedResponse.id);
    }

    final class ResponseFromStringArray extends DefaultHttpStatusHolder {
        public final String[] data;

        public ResponseFromStringArray(String[] data) {
            this.data = data;
        }
    }

    @Test
    void call_whenRegisterResponseMapper_shouldReturnExpectedData() throws JsonProcessingException {
        // arrange
        final String[] expectedResponse = new String[]{"data 1", "data 2"};
        final HttpStatus expectedResponseStatus = HttpStatus.OK;

        final DockingPoint<?> dockingPoint = new DockingPoint<>(createHttpResultProvider(), objectMapper)
                .register(expectedResponseStatus, String[].class, ResponseFromStringArray::new);
        // act
        final HttpStatusHolder actual = act(dockingPoint,
                when -> when.thenReturn(createResponseEntity(expectedResponseStatus, expectedResponse)));

        // assert
        assertThat(actual)
                .isNotNull();

        assertThat(actual.getHttpStatus())
                .isEqualTo(expectedResponseStatus);

        assertThat(actual)
                .isInstanceOf(ResponseFromStringArray.class);

        assertThat(((ResponseFromStringArray) actual).data)
                .isEqualTo(expectedResponse);
    }

    @Test
    void call_whenRegisterDefaultResponseMapper_shouldReturnExpectedData() throws JsonProcessingException {
        // arrange
        final String[] expectedResponse = new String[]{"data 1", "data 2"};
        final HttpStatus expectedResponseStatus = HttpStatus.OK;

        final DockingPoint<?> dockingPoint = new DockingPoint<>(createHttpResultProvider(), objectMapper)
                .registerDefault(String[].class, ResponseFromStringArray::new);

        // act
        final HttpStatusHolder actual = act(dockingPoint,
                when -> when.thenReturn(createResponseEntity(expectedResponseStatus, expectedResponse)));

        // assert
        assertThat(actual)
                .isNotNull();

        assertThat(actual.getHttpStatus())
                .isEqualTo(expectedResponseStatus);

        assertThat(actual)
                .isInstanceOf(ResponseFromStringArray.class);

        assertThat(((ResponseFromStringArray) actual).data)
                .isEqualTo(expectedResponse);
    }

    @Test
    void call_whenRegisterDefaultResponseMapperAndNotExpectedContentType_shouldThrowJsonProcessingException()  {
        // arrange
        final String[] expectedResponse = new String[]{"data 1", "data 2"};
        final HttpStatus expectedResponseStatus = HttpStatus.OK;

        final DockingPoint<?> dockingPoint = new DockingPoint<>(createHttpResultProvider(), objectMapper)
                .registerDefault(int.class, ResponseFromInt::new);

        // act
        assertThatThrownBy(() ->
                act(dockingPoint, when -> when.thenReturn(createResponseEntity(expectedResponseStatus, expectedResponse))))
                .isInstanceOf(JsonProcessingException.class);
    }

}
