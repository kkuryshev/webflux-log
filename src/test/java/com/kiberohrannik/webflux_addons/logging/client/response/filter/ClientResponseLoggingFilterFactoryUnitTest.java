package com.kiberohrannik.webflux_addons.logging.client.response.filter;

import com.kiberohrannik.webflux_addons.logging.base.BaseTest;
import com.kiberohrannik.webflux_addons.logging.client.LoggingProperties;
import com.kiberohrannik.webflux_addons.logging.client.response.message.BaseResponseMessageCreator;
import com.kiberohrannik.webflux_addons.logging.client.response.message.ResponseMessageCreator;
import com.kiberohrannik.webflux_addons.logging.client.response.message.formatter.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class ClientResponseLoggingFilterFactoryUnitTest extends BaseTest {

    @Test
    void createDefaultFilter() {
        LoggingProperties logProps = LoggingProperties.builder().build();

        ExchangeFilterFunction filterFunction = ClientResponseLoggingFilterFactory.defaultFilter(logProps);
        assertTrue(filterFunction instanceof ClientResponseLoggingFilter);

        ClientResponseLoggingFilter logFilter = (ClientResponseLoggingFilter) filterFunction;

        ResponseMessageCreator messageCreator = (ResponseMessageCreator) getField(logFilter, "messageCreator");
        assertTrue(messageCreator instanceof BaseResponseMessageCreator);

        List<ResponseDataMessageFormatter> messageFormatters =
                (List<ResponseDataMessageFormatter>) getField(messageCreator, "messageFormatters");

        assertNotNull(messageFormatters);
        assertEquals(4, messageFormatters.size());

        assertTrue(messageFormatters.stream().anyMatch(formatter -> formatter instanceof HeaderMessageFormatter));
        assertTrue(messageFormatters.stream().anyMatch(formatter -> formatter instanceof CookieMessageFormatter));
        assertTrue(messageFormatters.stream().anyMatch(formatter -> formatter instanceof ReqIdMessageFormatter));
        assertTrue(messageFormatters.stream().anyMatch(formatter -> formatter instanceof BodyMessageFormatter));
    }
}