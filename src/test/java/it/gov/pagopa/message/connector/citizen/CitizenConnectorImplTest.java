package it.gov.pagopa.message.connector.citizen;


import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CitizenConnectorImplTest {

    private MockWebServer mockWebServer;
    private CitizenConnectorImpl citizenConnector;
    private static final String FISCAL_CODE = "12345678901";
    private static final String RESPONSE = "OK";
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        citizenConnector = new CitizenConnectorImpl(mockWebServer.url("/").toString());
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

        Mono<String> resultMono = citizenConnector.checkFiscalCode(FISCAL_CODE);

        String result = resultMono.block();
        assertThat(result).isEqualTo(RESPONSE);
    }

}
