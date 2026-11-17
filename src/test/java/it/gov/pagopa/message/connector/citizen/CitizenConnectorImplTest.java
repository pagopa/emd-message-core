package it.gov.pagopa.message.connector.citizen;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import java.time.Duration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static it.gov.pagopa.message.utils.TestUtils.FISCAL_CODE;
import static it.gov.pagopa.message.utils.TestUtils.RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CitizenConnectorImplTest {

    private MockWebServer mockWebServer;
    private CitizenConnectorImpl citizenConnector;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Use the plain WebClient.Builder in tests — no Netty pool overhead needed.
        citizenConnector = new CitizenConnectorImpl(
                WebClient.builder(),
                mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testCheckFiscalCode() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(RESPONSE)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
                .expectNext(RESPONSE)
                .verifyComplete();
    }

    @Test
    void testCheckFiscalCode_retriesOnConnectionReset() {
        // First two calls fail with a network-level error (simulated by closing early),
        // third call succeeds — verifies the retryWhen(WebClientRequestException) policy.
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(RESPONSE)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
                .expectNext(RESPONSE)
                .verifyComplete();
    }

    @Test
    void testCheckFiscalCode_exhaustsRetriesAndPropagatesError() {
        for (int i = 0; i < 3; i++) {
            mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START));
        }

        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
            .expectErrorSatisfies(ex -> assertThat(ex)
                .isInstanceOfSatisfying(ClientExceptionWithBody.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.getCode()).isEqualTo("DOWNSTREAM_UNREACHABLE");
                }))
            .verify(Duration.ofSeconds(10));
    }

    @Test
    void testCheckFiscalCode_persistent503_exhaustsRetriesAndPropagatesError() {
        int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(503).setBody("{\"secret\":\"internal\"}"));
        }

        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
            .expectErrorSatisfies(ex -> assertThat(ex)
                .isInstanceOfSatisfying(ClientExceptionWithBody.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.getCode()).isEqualTo("DOWNSTREAM_ERROR");
                }))
            .verify(Duration.ofSeconds(10));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(attempts);
    }

    @Test
    void testCheckFiscalCode_4xxWithNonJsonBody_isNotRetried() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(422).setHeader("Content-Type", "text/plain").setBody("not-json"));

        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
            .expectErrorSatisfies(ex -> assertThat(ex)
                .isInstanceOfSatisfying(ClientExceptionWithBody.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getCode()).isEqualTo("INVALID_REQUEST");
                }))
            .verify(Duration.ofSeconds(10));

        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
