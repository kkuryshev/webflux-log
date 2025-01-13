package com.kv.webflux.logging.server.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kv.webflux.logging.server.app.TestDto;
import com.kv.webflux.logging.server.base.BaseIntegrationTest;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static com.kv.webflux.logging.server.app.TestController.RESPONSE_PREFIX;
import static com.kv.webflux.logging.server.app.TestController.TEST_ENDPOINT;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoggingFilterComponentTest extends BaseIntegrationTest {

    @Test
    void filter_whenReturned404_thenLogAllINREQExceptBody_andFullINRESP(CapturedOutput output) {
        String randomUri = "notExistingEndpoint";
        Mono<?> INRESPMono =
                createWebClient()
                        .post()
                        .uri(randomUri)
                        .retrieve()
                        .onStatus(
                                status -> status.equals(NOT_FOUND),
                                INRESP -> Mono.error(new MockException()))
                        .toBodilessEntity();

        assertThrows(MockException.class, INRESPMono::block);

        String logs = output.getAll();
        //        assertTrue(logs.contains("INREQ: POST http://localhost:8080/" + randomUri));
        //        assertEquals(2, StringUtils.countMatches(logs, " REQ-ID: [ TEST-REQ-ID_"));
        assertTrue(logs.contains(" HEADERS: [ "));
        //        assertTrue(logs.contains(" COOKIES: [ ]"));

        assertTrue(
                logs.contains(
                        " INRESP: POST http://localhost:8080/notExistingEndpoint  ELAPSED TIME: "));
        assertTrue(logs.contains(" STATUS: 404 Not Found"));
        //        assertTrue(logs.contains(" HEADERS: [ Content-Type=application/json"));
        assertTrue(logs.contains(" COOKIES (Set-Cookie): [ ] BODY: [ {no body} ]"));

        assertEquals(2, StringUtils.countMatches(logs, "BODY"));
    }

    @Test
    void filter_whenReturned400_thenLogFullINREQ_andFullINRESP(CapturedOutput output) {
        String invalidINREQBody = RandomString.make();

        Mono<?> INRESPMono =
                createWebClient()
                        .post()
                        .uri(TEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invalidINREQBody)
                        .retrieve()
                        .onStatus(
                                status -> status.equals(BAD_REQUEST),
                                INRESP -> Mono.error(new MockException()))
                        .toBodilessEntity();

        assertThrows(MockException.class, INRESPMono::block);

        String logs = output.getAll();
        //        assertTrue(logs.contains("INREQ: POST http://localhost:8080" + TEST_ENDPOINT));
        //        assertEquals(3, StringUtils.countMatches(logs, " REQ-ID: [ TEST-REQ-ID_"));
        assertTrue(logs.contains(" HEADERS: [ "));
        assertTrue(logs.contains(" COOKIES: [ ]"));
        assertTrue(logs.contains(" BODY: [ " + invalidINREQBody + " ]"));

        assertTrue(
                logs.contains(" INRESP: POST http://localhost:8080/test/endpoint  ELAPSED TIME: "));
        assertTrue(logs.contains(" STATUS: 400 Bad Request"));
        //        assertTrue(logs.contains(" HEADERS: [ Content-Type=application/json"));
        assertTrue(logs.contains(" COOKIES (Set-Cookie): [ ] BODY: [ {no body} ]"));
    }

    @Test
    void filter_whenReturned200_thenLogFullINREQ_andFullINRESP(CapturedOutput output)
            throws JsonProcessingException {

        TestDto INREQBody = new TestDto(RandomString.make(), RandomString.make());
        String validReqJson = new ObjectMapper().writeValueAsString(INREQBody);

        ResponseEntity<String> INRESP =
                createWebClient()
                        .post()
                        .uri(TEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(new ObjectMapper().writeValueAsString(INREQBody))
                        .retrieve()
                        .toEntity(String.class)
                        .block();

        assertNotNull(INRESP);
        assertEquals(HttpStatus.OK, INRESP.getStatusCode());

        String expectedINRESPBody = validReqJson + RESPONSE_PREFIX;
        assertEquals(expectedINRESPBody, INRESP.getBody());

        String logs = output.getAll();
        assertTrue(logs.contains("INREQ: POST http://localhost:8080" + TEST_ENDPOINT));
        //        assertEquals(3, StringUtils.countMatches(logs, " REQ-ID: [ TEST-REQ-ID_"));
        assertTrue(logs.contains(" HEADERS: [ "));
        assertTrue(logs.contains(" COOKIES: [ ]"));
        assertTrue(logs.contains(" BODY: [ " + validReqJson + "-RESPONSE ]"));
        assertTrue(
                logs.contains(" INRESP: POST http://localhost:8080/test/endpoint  ELAPSED TIME:"));
        assertTrue(logs.contains(" STATUS: 200 OK"));
        //        assertTrue(logs.contains(" HEADERS: [ Content-Type=application/json"));
        assertTrue(
                logs.contains(" COOKIES (Set-Cookie): [ ] BODY: [ " + expectedINRESPBody + " ]"));
    }

    public static class MockException extends RuntimeException {}
}
