package it.gov.pagopa.message.connector.citizen;

import it.gov.pagopa.message.custom.CitizenInvocationException;
import it.gov.pagopa.message.dto.CitizenConsentDTO;
import it.gov.pagopa.message.faker.CitizenConsentDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenConnectorTest {

    @InjectMocks
    private CitizenConnectorImpl citizenConnectorImpl;

    @Mock
    private CitizenFeignClient citizenFeignClient;

    private static final String HASHED_FISCAL_C0DE = "HASHED_FISCAL_C0DE";

    @Test
    void getCitizenConsentsEnabled_Success(){

        List<CitizenConsentDTO> citizenConsents = List.of(CitizenConsentDTOFaker.mockInstance(true));
        when(citizenFeignClient.getCitizenConsentsEnabled(HASHED_FISCAL_C0DE)).thenReturn(ResponseEntity.ok(citizenConsents));

        List<CitizenConsentDTO> result = citizenConnectorImpl.getCitizenConsentsEnabled(HASHED_FISCAL_C0DE).block();

        Assertions.assertNotNull(result);


        verify(citizenFeignClient, times(1)).getCitizenConsentsEnabled(HASHED_FISCAL_C0DE);
    }


    @Test
    void getCitizenConsentsEnabled_Exception(){
        when(citizenFeignClient.getCitizenConsentsEnabled(HASHED_FISCAL_C0DE))
                .thenThrow(new CitizenInvocationException());

        Mono<List<CitizenConsentDTO>> result = citizenConnectorImpl.getCitizenConsentsEnabled(HASHED_FISCAL_C0DE);

        CitizenInvocationException exception = assertThrows(CitizenInvocationException.class,
                result::block);

        Assertions.assertNotNull(exception);
    }
}
