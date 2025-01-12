package com.kv.webflux.logging.server.message.logger;

import com.kv.webflux.logging.provider.BodyProvider;
import com.kv.webflux.logging.server.exception.DataBufferCopyingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.FastByteArrayOutputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Objects;
import java.util.function.Supplier;

public class LoggingServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    private static final Log log = LogFactory.getLog(LoggingServerHttpResponseDecorator.class);

    private final BodyProvider provider = new BodyProvider();
    private final FastByteArrayOutputStream bodyOutputStream = new FastByteArrayOutputStream();

    public LoggingServerHttpResponseDecorator(
            ServerHttpResponse delegate, Supplier<String> sourceLogMessage) {
        super(delegate);

        delegate.beforeCommit(
                () -> {
                    String bodyMessage = provider.createBodyMessage(bodyOutputStream);
                    String fullLogMessage = sourceLogMessage.get().concat(bodyMessage);

                    logImpl(delegate, fullLogMessage);

                    return Mono.empty();
                });
    }

    private void logImpl(ServerHttpResponse response, String msg) {
        final var code = response.getStatusCode();

        if (Objects.isNull(code)) {
            log.warn(msg);
            return;
        }
        if (code.is2xxSuccessful() || code.is3xxRedirection()) {
            log.debug(msg);
        } else if (code.is4xxClientError() || code.is5xxServerError()) {
            log.error(msg);
        } else {
            log.warn(msg);
        }
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Flux<DataBuffer> bodyBufferWrapper =
                Flux.from(body).map(dataBuffer -> copyBodyBuffer(bodyOutputStream, dataBuffer));

        return super.writeWith(bodyBufferWrapper);
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        Flux<Flux<DataBuffer>> bodyBufferWrapper =
                Flux.from(body)
                        .map(
                                publisher ->
                                        Flux.from(publisher)
                                                .map(
                                                        buffer ->
                                                                copyBodyBuffer(
                                                                        bodyOutputStream, buffer)));

        return super.writeAndFlushWith(bodyBufferWrapper);
    }

    private DataBuffer copyBodyBuffer(FastByteArrayOutputStream bodyStream, DataBuffer buffer) {
        try {
            Channels.newChannel(bodyStream).write(buffer.asByteBuffer().asReadOnlyBuffer());

            return buffer;

        } catch (IOException e) {
            throw new DataBufferCopyingException(e);
        }
    }
}
