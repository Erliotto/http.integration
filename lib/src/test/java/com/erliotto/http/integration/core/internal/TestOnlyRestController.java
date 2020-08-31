package com.erliotto.http.integration.core.internal;

import com.erliotto.http.integration.core.DefaultHttpStatusHolder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestOnlyRestController {

    public static class ReturnTypes {
        public static final class Json extends DefaultHttpStatusHolder {
            @JsonProperty("id")
            public final int id;

            @JsonProperty("name")
            public final String name;

            @JsonCreator
            public Json(@JsonProperty("id") int id,
                        @JsonProperty("name") String name) {
                this.id = id;
                this.name = name;
            }
        }
    }

    @GetMapping(value = "getString")
    public String getString() {
        return "just string value";
    }

    @GetMapping(value = "getStringArray")
    public String[] getStringArray() {
        return new String[]{
                "item 1",
                "item 2",
        };
    }

    @GetMapping(value = "getJson", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnTypes.Json getJson() {
        return new ReturnTypes.Json(102, "json name");
    }
}
