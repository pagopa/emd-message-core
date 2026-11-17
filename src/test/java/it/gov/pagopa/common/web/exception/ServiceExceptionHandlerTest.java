package it.gov.pagopa.common.web.exception;

import ch.qos.logback.classic.LoggerContext;
import it.gov.pagopa.common.utils.MemoryAppender;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@WebFluxTest(value = {
        ServiceExceptionHandlerTest.TestController.class})
@ContextConfiguration(classes = {ServiceExceptionHandler.class,
        ServiceExceptionHandlerTest.TestController.class, ErrorManager.class})
class ServiceExceptionHandlerTest {
    @Autowired
    private WebTestClient webTestClient;

    private static MemoryAppender memoryAppender;

    @BeforeAll
    static void configureMemoryAppender(){
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        memoryAppender.start();
    }

    @BeforeEach
    void clearMemoryAppender(){
        memoryAppender.reset();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ErrorManager.class.getName());
        logger.setLevel(ch.qos.logback.classic.Level.INFO);
        logger.addAppender(memoryAppender);
    }

    @RestController
    @Slf4j
    static class TestController {

        @GetMapping("/test")
        String test() {
            throw new ServiceException("DUMMY_CODE", "DUMMY_MESSAGE");
        }

        @GetMapping("/test/customBody")
        String testCustomBody() {
            throw new ServiceException("DUMMY_CODE", "DUMMY_MESSAGE", new ErrorPayloadTest("RESPONSE",0), true, null);
        }

        @GetMapping("/test/downstream/{status}")
        String testDownstream(
            @PathVariable
            int status,
            @RequestParam(required = false)
            String body) {
            throw WebClientResponseException.create(
                status,
                "Downstream error",
                HttpHeaders.EMPTY,
                body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
        }

        @GetMapping("/test/downstream-unreachable")
        String testDownstreamUnreachable() {
            throw new WebClientRequestException(
                new ConnectException("Connection refused"),
                HttpMethod.GET,
                URI.create("http://internal-host:8080/secret/path"),
                HttpHeaders.EMPTY);
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ErrorPayloadTest implements ServiceExceptionPayload {
        private String stringCode;
        private long longCode;
    }

    @Test
    void testSimpleException(){
        webTestClient.method(HttpMethod.GET)
                .uri("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("DUMMY_CODE")
                .jsonPath("$.message").isEqualTo("DUMMY_MESSAGE");

        ErrorManagerTest.checkStackTraceSuppressedLog(memoryAppender, "A ServiceException occurred handling request GET /test: HttpStatus 500 INTERNAL_SERVER_ERROR - DUMMY_CODE: DUMMY_MESSAGE at it.gov.pagopa.common.web.exception.ServiceExceptionHandlerTest\\$TestController.test\\(ServiceExceptionHandlerTest.java:[0-9]+\\)");

    }

    @Test
    void testCustomBodyException(){

        webTestClient.method(HttpMethod.GET)
                .uri("/test/customBody")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.stringCode").isEqualTo("RESPONSE")
                .jsonPath("$.longCode").isEqualTo(0);

        ErrorManagerTest.checkLog(memoryAppender,
                "Something went wrong handling request GET /test/customBody: HttpStatus 500 INTERNAL_SERVER_ERROR - DUMMY_CODE: DUMMY_MESSAGE",
                "it.gov.pagopa.common.web.exception.ServiceException: DUMMY_MESSAGE",
                "it.gov.pagopa.common.web.exception.ServiceExceptionHandlerTest$TestController.testCustomBody"

        );
    }

    @Test
    void testHandleDownstream4xxPropagatesCodeAndMessage() {
        String downstreamBody = """
            {"code": "INVALID_REQUEST", "message": "[fiscalCode]: Invalid fiscal code format"}""";

        webTestClient.get()
            .uri("/test/downstream/{status}?body={body}", 400, downstreamBody)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
            .jsonPath("$.message").isEqualTo("[fiscalCode]: Invalid fiscal code format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-json", "{\"foo\": \"bar\"}"})
    void testHandleDownstream4xxFallback(String downstreamBody) {
        webTestClient.get()
            .uri("/test/downstream/{status}?body={body}", 422, downstreamBody)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.code").isEqualTo("INVALID_REQUEST")
            .jsonPath("$.message").isEqualTo("Request rejected by downstream service");
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503})
    void testHandleDownstream5xx(int downstreamStatus) {
        webTestClient.get()
            .uri("/test/downstream/{status}?body={body}", downstreamStatus, "{\"secret\": \"internal\"}")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY)
            .expectBody()
            .jsonPath("$.code").isEqualTo("DOWNSTREAM_ERROR")
            .jsonPath("$.message").isEqualTo("Downstream service error");
    }

    @Test
    void testHandleDownstreamUnreachable() {
        webTestClient.get()
            .uri("/test/downstream-unreachable")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY)
            .expectBody()
            .jsonPath("$.code").isEqualTo("DOWNSTREAM_UNREACHABLE")
            .jsonPath("$.message").isEqualTo("Downstream service unreachable");
    }
}
