package it.gov.pagopa.message.connector.citizen;


import it.gov.pagopa.message.connector.CitizenConnectorImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;

import static it.gov.pagopa.message.utils.TestUtils.FISCAL_CODE;
import static it.gov.pagopa.message.utils.TestUtils.RESPONSE;

@ExtendWith(MockitoExtension.class)
class CitizenConnectorImplTest {

    private MockWebServer mockWebServer;
    private CitizenConnectorImpl citizenConnector;
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


        StepVerifier.create(citizenConnector.checkFiscalCode(FISCAL_CODE))
                .expectNext(RESPONSE)
                .verifyComplete();
    }

}
