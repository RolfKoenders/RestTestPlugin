package nl.rolflab.resttest;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;

public class AuthenticatingPathHandler extends PathHandler {
    private final ApiKeyAuthMechanism apiKeyAuthMechanism;

    public AuthenticatingPathHandler(ApiKeyAuthMechanism apiKeyAuthMechanism) {
        this.apiKeyAuthMechanism = apiKeyAuthMechanism;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (apiKeyAuthMechanism.authenticate(exchange, exchange.getSecurityContext()) == AuthenticationMechanism.AuthenticationMechanismOutcome.AUTHENTICATED) {
            super.handleRequest(exchange);
        } else {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Unauthorized: Invalid API key");
        }
    }
}
