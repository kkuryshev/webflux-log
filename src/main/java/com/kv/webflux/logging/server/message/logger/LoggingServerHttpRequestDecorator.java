package com.kv.webflux.logging.server.message.logger;

import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.provider.BodyProvider;
import com.kv.webflux.logging.server.exception.DataBufferCopyingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.FastByteArrayOutputStream;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.channels.Channels;

public class LoggingServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private static final Log log = LogFactory.getLog(LoggingServerHttpRequestDecorator.class);

    private final BodyProvider bodyProvider = new BodyProvider();
    private final String requestInfo;
    private String fullBodyMessage;

    public LoggingServerHttpRequestDecorator(
            ServerHttpRequest delegate, LoggingProperties loggingProperties, String requestInfo) {
        super(delegate);
        this.requestInfo = requestInfo;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody()
                .switchIfEmpty(
                        Flux.<DataBuffer>empty()
                                .doOnComplete(
                                        () ->
                                                log.debug(
                                                        requestInfo.concat(
                                                                bodyProvider
                                                                        .createNoBodyMessage()))))
                .doOnNext(
                        dataBuffer -> {
                            this.fullBodyMessage =
                                    bodyProvider.createBodyMessage(copyBodyBuffer(dataBuffer));
                            log.debug(requestInfo.concat(this.fullBodyMessage));
                        });
    }

    private FastByteArrayOutputStream copyBodyBuffer(DataBuffer buffer) {
        try {
            FastByteArrayOutputStream bodyStream = new FastByteArrayOutputStream();
            Channels.newChannel(bodyStream).write(buffer.asByteBuffer().asReadOnlyBuffer());

            return bodyStream;

        } catch (IOException e) {
            throw new DataBufferCopyingException(e);
        }
    }

    public String getFullBodyMessage(){
        return fullBodyMessage;
    }
}
