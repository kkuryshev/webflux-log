package com.kv.webflux.logging.server.message.logger;

import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.provider.BodyProvider;
import com.kv.webflux.logging.provider.HttpStatusProvider;
import com.kv.webflux.logging.provider.TimeElapsedProvider;
import com.kv.webflux.logging.server.message.formatter.ServerMetadataMessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class DefaultServerResponseLogger implements ServerResponseLogger {

    private static final Log log = LogFactory.getLog(DefaultServerResponseLogger.class);

    private final LoggingProperties properties;
    private final List<ServerMetadataMessageFormatter> messageFormatters;

    private final HttpStatusProvider statusProvider = new HttpStatusProvider();
    private final TimeElapsedProvider timeProvider = new TimeElapsedProvider();

    public DefaultServerResponseLogger(
            LoggingProperties properties, List<ServerMetadataMessageFormatter> messageFormatters) {
        this.properties = properties;
        this.messageFormatters = messageFormatters;
    }

    @Override
    public ServerHttpResponse log(ServerWebExchange exchange, long exchangeStartTimeMillis) {
        Supplier<String> msgSupplier = createMetadataMessage(exchange, exchangeStartTimeMillis);

        if (properties.isLogBody()) {
            return new LoggingServerHttpResponseDecorator(exchange, msgSupplier);

        } else {
            exchange.getResponse()
                    .beforeCommit(
                            () -> Mono.fromRunnable(() -> logImpl(exchange, msgSupplier.get())));
            return exchange.getResponse();
        }
    }

    public static void logImpl(ServerWebExchange exchange, String msg) {
        final var code = exchange.getResponse().getStatusCode();
        final BodyProvider provider = new BodyProvider();
        if (Objects.isNull(code)) {
            log.warn(msg);
            return;
        }
        if (code.is2xxSuccessful() || code.is3xxRedirection()) {
            log.debug(msg);
        } else if (code.is4xxClientError() || code.is5xxServerError()) {
            if (true && exchange.getRequest() instanceof LoggingServerHttpRequestDecorator req) {
                log.error(
                        String.format(
                                "%s , request body was %s",
                                msg, provider.createBodyMessage(req.getFullBodyMessage(), false)));
            } else {
                log.error(msg);
            }

        } else {
            log.warn(msg);
        }
    }

    private Supplier<String> createMetadataMessage(
            ServerWebExchange exchange, long exchangeStartTimeMillis) {
        return () -> {
            final var status = exchange.getResponse().getStatusCode();
            String statusValue = statusProvider.createMessage(status.value());
            StringBuilder metadata = new StringBuilder(statusValue);

            String uri =
                    String.join(
                            " ",
                            exchange.getRequest().getMethod().toString(),
                            exchange.getRequest().getURI().toString());

            for (ServerMetadataMessageFormatter formatter : messageFormatters) {
                metadata.append(formatter.formatMessage(exchange, properties));
            }

            String timeElapsed =
                    timeProvider.createMessage(
                            System.currentTimeMillis() - exchangeStartTimeMillis);
            return String.join(" ", "INRESP:", uri, timeElapsed, metadata.toString());
        };
    }
}
