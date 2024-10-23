package it.gov.pagopa.message.connector.tpp;

import it.gov.pagopa.message.custom.TppInvocationException;
import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import it.gov.pagopa.message.faker.TppDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppConnectorTest {

    @InjectMocks
    private TppConnectorImpl tppConnectorImpl;

    @Mock
    private TppFeignClient tppFeignClient;

    @Test
    void getTppsEnabled_Success() {
        TppIdList tppIdList = new TppIdList();
        List<TppDTO> tppDTOs =List.of(TppDTOFaker.mockInstance());
        when(tppFeignClient.getTppsEnabled(tppIdList)).thenReturn(tppDTOs);

        List<TppDTO> result = tppConnectorImpl.getTppsEnabled(tppIdList).block();

        Assertions.assertNotNull(result);
        assertFalse(result.isEmpty());
        Assertions.assertEquals(tppDTOs.size(), result.size());

        verify(tppFeignClient, times(1)).getTppsEnabled(tppIdList);
    }

    @Test
    void getTppsEnabled_Exception() {
        TppIdList tppIdList = new TppIdList();

        when(tppFeignClient.getTppsEnabled(tppIdList))
                .thenThrow(new TppInvocationException());

        Mono<List<TppDTO>> result = tppConnectorImpl.getTppsEnabled(tppIdList);

        TppInvocationException exception = assertThrows(TppInvocationException.class, result::block);

        Assertions.assertNotNull(exception);

    }
}
