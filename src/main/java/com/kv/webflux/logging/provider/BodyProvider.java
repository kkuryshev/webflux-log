package com.kv.webflux.logging.provider;

import com.kv.webflux.logging.client.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.FastByteArrayOutputStream;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

public final class BodyProvider {

    public Mono<String> createBodyMessage(Mono<String> bodyMono) {
        return bodyMono.defaultIfEmpty(LoggingUtils.NO_BODY_MESSAGE).map(this::createBodyMessage);
    }

    public String createBodyMessage(FastByteArrayOutputStream bodyOutputStream) {
        return createBodyMessage(bodyOutputStream.toString());
    }

    public String createBodyMessage(DataBuffer bodyDataBuffer) {
        return createBodyMessage(bodyDataBuffer.toString(Charset.defaultCharset()));
    }

    public String createBodyMessage(String body) {
        return createBodyMessage(body, true);
    }

    public String createBodyMessage(String body, boolean truncate) {
        return StringUtils.isNotBlank(body) ? create(body, truncate) : createNoBodyMessage();
    }

    public String createNoBodyMessage() {
        return create(LoggingUtils.NO_BODY_MESSAGE, false);
    }

    private String create(String body, boolean truncate) {
        final var truncateBody =
                truncate ? body.length() > 100 ? StringUtils.left(body, 100) + "..." : body : body;
        return " BODY: [ ".concat(truncateBody).concat(" ]");
    }
}
