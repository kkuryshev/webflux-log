package com.kv.webflux.logging.provider;

import com.kv.webflux.logging.base.BaseTest;
import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.client.LoggingUtils;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class HeaderProviderUnitTest extends BaseTest {

    private final HeaderProvider provider = new HeaderProvider();

    @Test
    void createMessage_whenNoMasked_thenAddAll() {
        String headerName0 = RandomString.make();
        String headerValue0 = RandomString.make();

        String headerName1 = RandomString.make();
        String headerValue1 = RandomString.make();

        String headerValue2 = RandomString.make();

        LoggingProperties logProps = LoggingProperties.builder().logHeaders(true).build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(headerName0, headerValue0);
        headers.add(headerName1, headerValue1);
        headers.add(headerName0, headerValue2);

        String actual = provider.createMessage(headers, logProps);
        log.info(actual);

        assertAll(
                () -> assertTrue(actual.contains(" HEADERS: [ ")),
                () -> assertTrue(actual.contains(headerName0 + "=" + headerValue0)),
                () -> assertTrue(actual.contains(headerName1 + "=" + headerValue1)),
                () -> assertTrue(actual.contains(headerName0 + "=" + headerValue2)));
    }

    @Test
    void createMessage_whenMasked_thenAddWithMask() {
        String headerName0 = RandomString.make();
        String headerValue0 = RandomString.make();

        String headerName1 = RandomString.make();
        String headerValue1 = RandomString.make();

        String headerName2 = RandomString.make();
        String headerValue2 = RandomString.make();

        String headerValue3 = RandomString.make();

        String notExistingHeaderName = RandomString.make();

        LoggingProperties logProps =
                LoggingProperties.builder()
                        .logHeaders(true)
                        .maskedHeaders(headerName0, headerName1, notExistingHeaderName)
                        .build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(headerName0, headerValue0);
        headers.add(headerName1, headerValue1);
        headers.add(headerName2, headerValue2);
        headers.add(headerName0, headerValue3);

        String actual = provider.createMessage(headers, logProps);
        log.info(actual);

        assertAll(
                () -> assertTrue(actual.contains(" HEADERS: [ ")),
                () -> assertFalse(actual.contains(headerName0 + "=" + headerValue0)),
                () ->
                        assertEquals(
                                2,
                                StringUtils.countMatches(
                                        actual, headerName0 + "=" + LoggingUtils.DEFAULT_MASK)),
                () -> assertFalse(actual.contains(headerName1 + "=" + headerValue1)),
                () -> assertTrue(actual.contains(headerName1 + "=" + LoggingUtils.DEFAULT_MASK)),
                () -> assertFalse(actual.contains(headerName0 + "=" + headerValue3)),
                () -> assertTrue(actual.contains(headerName2 + "=" + headerValue2)),
                () -> assertFalse(actual.contains(notExistingHeaderName)));
    }

    @Test
    void createMessage_whenWithCookies_thenRemoveItFromLog() {
        String headerName0 = "CoOkIe";
        String headerValue0 = RandomString.make();

        String headerName1 = HttpHeaders.COOKIE.toUpperCase(Locale.ROOT);
        String headerValue1 = RandomString.make();

        String headerName2 = HttpHeaders.SET_COOKIE;
        String headerValue2 = RandomString.make();

        LoggingProperties properties = LoggingProperties.builder().logHeaders(true).build();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(headerName0, headerValue0);
        headers.add(headerName1, headerValue1);
        headers.add(headerName2, headerValue2);

        String actual = provider.createMessage(headers, properties);
        log.info(actual);

        assertAll(
                () -> assertTrue(actual.contains(" HEADERS: [ ")),
                () -> assertFalse(actual.contains(headerName0 + "=" + headerValue0)),
                () -> assertFalse(actual.contains(headerName1 + "=" + headerValue1)),
                () -> assertFalse(actual.contains(headerName0 + "=" + headerValue2)));
    }
}
