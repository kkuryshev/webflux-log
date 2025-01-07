package com.kv.webflux.logging.provider;

import com.kv.webflux.logging.base.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.kv.webflux.logging.util.TestUtils.getRandomEnumValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpStatusProviderUnitTest extends BaseTest {

    private final HttpStatusProvider provider = new HttpStatusProvider();

    @Test
    void createMessage_whenNullHttpStatus_thenAddNull() {
        Integer nullStatus = null;

        String actual = provider.createMessage(nullStatus);
        log.info(actual);

        assertEquals(" STATUS: " + nullStatus, actual);
    }

    @Test
    void createMessage_whenGeneralHttpStatus_thenAddCodeWithMessage() {
        HttpStatus httpStatus = getRandomEnumValue(HttpStatus.class);
        Integer statusCode = httpStatus.value();

        String actual = provider.createMessage(statusCode);
        log.info(actual);

        assertEquals(" STATUS: " + statusCode + " " + httpStatus.getReasonPhrase(), actual);
    }

    @Test
    void createMessage_whenRawHttpStatus_thenAddCodeOnly() {
        Integer statusCode = 220;

        String actual = provider.createMessage(statusCode);
        log.info(actual);

        assertEquals(" STATUS: " + statusCode, actual);
    }
}
