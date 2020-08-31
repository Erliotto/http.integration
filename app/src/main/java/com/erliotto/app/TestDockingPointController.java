package com.erliotto.app;

import com.erliotto.http.integration.core.DefaultHttpStatusHolder;
import com.erliotto.http.integration.core.DockingPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TestDockingPointController {

    private final DockingPoint<StringResponse> dockingPoint;

    @Autowired
    public TestDockingPointController(
            DockingPoint<StringResponse> dockingPoint
    ) {
        this.dockingPoint = dockingPoint;
    }

    @GetMapping(value = "ping")
    public String ping() {
        dockingPoint.registerDefault(String.class, rawResponse -> new StringResponse(rawResponse));

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final String url = request.getRequestURL().toString().replaceFirst("ping", "test");
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

    private static final class StringResponse extends DefaultHttpStatusHolder {
        public final String data;

        StringResponse(String data) {
            this.data = data;
        }
    }
}
