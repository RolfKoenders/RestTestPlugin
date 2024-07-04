package nl.rolflab.resttest;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import util.HttpUtils;

public class ApiKeyAuthMechanism implements AuthenticationMechanism {

    private final String apiKey;

    public ApiKeyAuthMechanism(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange httpServerExchange, SecurityContext securityContext) {
        String providedApiKey = HttpUtils.extractApiKeyFromAuthorizationHeader(httpServerExchange);
        assert providedApiKey != null;
        if (providedApiKey.equals(apiKey)) {
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        }
        return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange httpServerExchange, SecurityContext securityContext) {
        return null;
    }

}
