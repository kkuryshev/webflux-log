package com.kv.webflux.logging.server.message.formatter.response;

import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.provider.HeaderProvider;
import com.kv.webflux.logging.server.message.formatter.ServerMetadataMessageFormatter;
import org.springframework.web.server.ServerWebExchange;

public final class HeaderServerResponseFormatter implements ServerMetadataMessageFormatter {

    private final HeaderProvider provider = new HeaderProvider();

    @Override
    public String formatMessage(ServerWebExchange exchange, LoggingProperties properties) {
        return provider.createMessage(exchange.getResponse().getHeaders(), properties);
    }
}
