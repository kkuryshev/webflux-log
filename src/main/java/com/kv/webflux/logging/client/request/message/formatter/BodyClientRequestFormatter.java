package com.kv.webflux.logging.client.request.message.formatter;

import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.client.LoggingUtils;
import com.kv.webflux.logging.client.request.message.formatter.extractor.RequestBodyExtractor;
import com.kv.webflux.logging.provider.BodyProvider;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

public class BodyClientRequestFormatter {

    private final RequestBodyExtractor bodyExtractor = new RequestBodyExtractor();
    private final BodyProvider bodyProvider = new BodyProvider();

    public Mono<String> formatMessage(ClientRequest request, LoggingProperties properties) {
        return properties.isLogBody()
                ? bodyProvider.createBodyMessage(bodyExtractor.extractBody(request))
                : Mono.just(LoggingUtils.EMPTY_MESSAGE);
    }
}
