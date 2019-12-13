package io.dropwizard.primer.utils;

import com.google.common.base.Strings;
import io.dropwizard.primer.model.DynamicTokenDetails;
import io.dropwizard.primer.model.PrimerHttpHeaders;
import io.dropwizard.primer.model.PrimerTokenDetails;
import io.dropwizard.primer.model.PrimerTokenDetails.TokenType;
import io.dropwizard.primer.model.StaticTokenDetails;

import javax.ws.rs.core.HttpHeaders;
import java.util.*;

import static io.dropwizard.primer.model.PrimerTokenDetails.TokenType.DYNAMIC;
import static io.dropwizard.primer.model.PrimerTokenDetails.TokenType.STATIC;

public interface PrimerHeaderUtils {

    /**
     * Util method to get primer token details if populated by primer filter
     *
     * @param httpHeaders
     * @return
     */
    static PrimerTokenDetails primerTokenDetails(HttpHeaders httpHeaders) {
        Map<String, List<String>> headers = new HashMap<>();
        httpHeaders.getRequestHeaders().forEach((key, list) -> {
            headers.put(key, new ArrayList<>(list));
        });

        return primerTokenDetails(headers);
    }

    static PrimerTokenDetails primerTokenDetails(Map<String, List<String>> headers) {
        TokenType tokenType = resolvePrimerTokenType(headers);

        if (Objects.isNull(tokenType)) {
            return null;
        }

        switch (tokenType) {
            case DYNAMIC:
                return DynamicTokenDetails.dynamicTokenBuilder()
                        .id(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_ID))
                        .name(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_NAME))
                        .subject(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_SUBJECT))
                        .role(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_ROLE))
                        .build();
            case STATIC:
                return StaticTokenDetails.staticTokenBuilder()
                        .role(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_ROLE))
                        .subject(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_SUBJECT))
                        .build();
            default:
                throw new UnsupportedOperationException("primer token type not supported");
        }
    }

    static TokenType resolvePrimerTokenType(Map<String, List<String>> headers) {
        String tokenType = getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZATION_TOKEN_TYPE);
        if (!Strings.isNullOrEmpty(tokenType)) {
            return TokenType.valueOf(tokenType);
        }

        // TODO: Hack to support old requests for which AUTHORIZATION_TOKEN_TYPE ain't present
        //  remove this hack after we've started populating AUTHORIZATION_TOKEN_TYPE in headers
        if (Strings.isNullOrEmpty(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_ID))
                && !Strings.isNullOrEmpty(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_SUBJECT))) {
            return STATIC;
        } else if (!Strings.isNullOrEmpty(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_ID))
                && !Strings.isNullOrEmpty(getHeaderParam(headers, PrimerHttpHeaders.AUTHORIZED_FOR_SUBJECT))) {
            return DYNAMIC;
        } else {
            return null;
        }
    }

    static String getHeaderParam(Map<String, List<String>> headers, String param) {
        return headers.getOrDefault(param, Collections.emptyList()).isEmpty()
                ? null
                : headers.get(param).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }
}
