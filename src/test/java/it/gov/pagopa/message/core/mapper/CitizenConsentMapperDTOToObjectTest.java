package it.gov.pagopa.message.core.mapper;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.faker.CitizenConsentDTOFaker;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.model.mapper.CitizenConsentMapperDTOToObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CitizenConsentMapperDTOToObjectTest {
    private CitizenConsentMapperDTOToObject mapper;
    @BeforeEach
    void setUp() {
        mapper = new CitizenConsentMapperDTOToObject();
    }

    @Test
    void transactionMapperTest() {
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);
        CitizenConsent result = mapper.citizenConsentDTOMapper(citizenConsentDTO);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(citizenConsentDTO.getChannelState(), result.getChannelState());
            assertEquals(citizenConsentDTO.getHashedFiscalCode(), result.getHashedFiscalCode());
            assertEquals(citizenConsentDTO.getUserId(), result.getUserId());
            assertEquals(citizenConsentDTO.getCreationDate(), result.getCreationDate());
            assertEquals(citizenConsentDTO.getLastUpdateDate(), result.getLastUpdateDate());
            assertEquals(citizenConsentDTO.getChannelId(), result.getChannelId());
        });
    }


}
