package util;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class HttpUtils {

    public static String extractApiKeyFromAuthorizationHeader(HttpServerExchange exchange) {
        String authorizationHeader = exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return null;
    }

}