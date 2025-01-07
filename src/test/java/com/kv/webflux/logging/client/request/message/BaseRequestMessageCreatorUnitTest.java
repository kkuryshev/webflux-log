package com.kv.webflux.logging.client.request.message;

import com.kv.webflux.logging.base.BaseTest;
import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.client.request.message.formatter.CookieClientRequestFormatter;
import com.kv.webflux.logging.client.request.message.formatter.HeaderClientRequestFormatter;
import com.kv.webflux.logging.client.request.message.formatter.ReqIdClientRequestFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BaseRequestMessageCreatorUnitTest extends BaseTest {

    private RequestMessageCreator requestMessageCreator;

    @Spy private ReqIdClientRequestFormatter reqIdFormatter;

    @Spy private HeaderClientRequestFormatter headerFormatter;

    @Spy private CookieClientRequestFormatter cookieFormatter;

    private final LoggingProperties properties =
            LoggingProperties.builder()
                    .logHeaders(true)
                    .logCookies(true)
                    .logRequestId(true)
                    .build();

    @BeforeEach
    void setUp() {
        requestMessageCreator =
                new BaseRequestMessageCreator(
                        properties, List.of(reqIdFormatter, headerFormatter, cookieFormatter));
    }

    @Test
    void formatMessage_usingInjectedFormatters() {
        ClientRequest testRequest =
                ClientRequest.create(HttpMethod.GET, URI.create("/someUri"))
                        .header(HttpHeaders.AUTHORIZATION, "Some Auth")
                        .cookie("Session", "sid4567")
                        .build();

        String result = requestMessageCreator.createMessage(testRequest).block();

        assertNotNull(result);
        assertTrue(result.contains("OUTREQ:"));
        assertTrue(result.contains(testRequest.method().name()));
        assertTrue(result.contains(testRequest.url().toString()));

        assertTrue(result.contains(testRequest.logPrefix().replaceAll("[\\[\\]\\s]", "")));
        assertTrue(result.contains(HttpHeaders.AUTHORIZATION + "=Some Auth"));
        assertTrue(result.contains("Session=sid4567"));

        verify(reqIdFormatter).formatMessage(testRequest, properties);
        verify(headerFormatter).formatMessage(testRequest, properties);
        verify(cookieFormatter).formatMessage(testRequest, properties);
    }
}
