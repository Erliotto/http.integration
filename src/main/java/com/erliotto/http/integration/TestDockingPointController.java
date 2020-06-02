package com.erliotto.http.integration;

import com.erliotto.http.integration.core.DefaultHttpStatusHolder;
import com.erliotto.http.integration.core.DockingPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDockingPointController {

    static final class StringResponse extends DefaultHttpStatusHolder {
        public final String data;

        StringResponse(String data) {
            this.data = data;
        }
    }

    @Autowired
    DockingPoint<StringResponse> dockingPoint;

    @GetMapping(value = "ping")
    public String ping() {
        dockingPoint.registerDefault(String.class, rawResponse -> new StringResponse(rawResponse));

        final String url = "http://localhost:8080/test";
        try {
            final StringResponse result = dockingPoint.call(HttpMethod.GET, url, null, null);

            System.out.println(String.format("Status: %s, data: %s", result.getHttpStatus(), result.data));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "pong";
    }

    @GetMapping(value = "test")
    public String test() {
        return "test content value";
    }
}
