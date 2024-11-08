package it.gov.pagopa.message.connector.citizen;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


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
 void testCheckFiscalCode() throws JsonProcessingException {
    String fiscalCode = "12345678901";
    String response = "OK";
    mockWebServer.enqueue(new MockResponse()
           .setResponseCode(200)
           .setBody(response)
           .addHeader("Content-Type", "application/json"));

    Mono<String> resultMono = citizenConnector.checkFiscalCode(fiscalCode);

    String result = resultMono.block();
    assertThat(result).isEqualTo(response);
 }

}
