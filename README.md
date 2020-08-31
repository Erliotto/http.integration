# http.integration

**Create *http integration*  in Java 8 code:**

This project is just a toy, for production code please use https://github.com/OpenFeign/feign.

```java
@RestController
public class TestDockingPointController {

    // 1) declare dockingPoint
    private final DockingPoint<StringResponse> dockingPoint;

    // 2) inject dockingPoint
    @Autowired
    public TestDockingPointController(
            DockingPoint<StringResponse> dockingPoint
    ) {
        this.dockingPoint = dockingPoint;
    }

    // 3) test
    @GetMapping(value = "ping")
    public String ping() {
        // 3.1 register all responses as String
        dockingPoint.registerDefault(String.class, rawResponse -> new StringResponse(rawResponse));

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final String url = request.getRequestURL().toString().replaceFirst("ping", "test");
        try {
            // 3.2 call itself 
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
```

