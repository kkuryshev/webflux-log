package com.kiberohrannik.webflux_addons.temp;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
//import com.kiberohrannik.webflux_addons.logging.creator.RequestBodyExtractor;
import com.kiberohrannik.webflux_addons.logging.creator.RequestBodyMapper;
import com.kiberohrannik.webflux_addons.logging.creator.RequestMessageCreator;
import com.kiberohrannik.webflux_addons.logging.filter.BaseLogRequestFilter;
import com.kiberohrannik.webflux_addons.logging.filter.LoggingProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@ActiveProfiles("test")
@SpringBootTest
@SpringBootConfiguration
public class WebClientTest {

    private static WireMockServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = new WireMockServer(8080);

        mockServer.start();
        WireMock.configureFor(8080);
    }

    @AfterEach
    void stop() {
        mockServer.stop();
    }


    @Test
    void test() throws InterruptedException {
//        WebClient.builder()
//                .filters() //Consumer<List<ExchangeFilterFunction>> filtersConsumer

        WireMock.stubFor(WireMock.post("/some")
                .withRequestBody(WireMock.containing(""))
                .willReturn(WireMock.status(200)));

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
//                .filter(new BaseLogRequestFilter(new RequestMessageCreator(new RequestBodyExtractor(new RequestBodyMapper())))
//                        .logRequest(LoggingProperties.builder()
//                                .logHeaders(true)
//                                .logCookies(true)
//                                .logBody(true)
//                                .build()))
                .build();

        System.out.println("\n\n");

        webClient.post()
                .uri("/some")

                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer gtrwghtuiwut542h0jutnui")

                .cookie("Cookie-1", "1111") //TODO add sensitive headers,  sensitive cookies (exceptions)
                .cookie("Cookie-2", "2222")

                .attribute("Attribute-1", "AAAA")
                .attribute("Attribute-2", "BBBB")

//                .body(Mono.just("some body 1234567890 (Mono.just - String)"), String.class)
//                .body(Mono.just(new SomeDto("some body 1234567890 (Mono.just - SomeDto)")), SomeDto.class)
//                .body(Mono.fromSupplier(() -> "olala -- fromSupplier"), String.class)
//                .body(Mono.fromSupplier(() -> "olala -- fromSupplier - ParameterizedTypeReference"), new ParameterizedTypeReference<String>() {})
//                .bodyValue("some body 1234567890")

                //TODO
//                .body(BodyInserters.fromPublisher(Mono.just("some body 1234567890 -- BodyInserters"), String.class))
                .body(BodyInserters.fromPublisher(Flux.just("val1", "val2", "val3"), String.class))

                //TODO
//                .body(producer)

                .retrieve()
                .toBodilessEntity()

                .subscribe(System.out::println);

        System.out.println("\n\n");

        TimeUnit.SECONDS.sleep(2);
    }


    @Data
    @AllArgsConstructor
    public static class SomeDto {
        private String name;
    }
}