package it.gov.pagopa.message.core.mapper;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.dto.mapper.CitizenConsentMapperObjectToDTO;
import it.gov.pagopa.message.core.faker.CitizenConsentFaker;
import it.gov.pagopa.message.core.model.CitizenConsent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class CitizenConsentMapperObjectToDTOTest {

    private CitizenConsentMapperObjectToDTO mapper;
    @BeforeEach
    void setUp() {
        mapper = new CitizenConsentMapperObjectToDTO();
    }

    @Test
    void transactionMapperTest() {
        CitizenConsent citizenConsent = CitizenConsentFaker.mockInstance(true);
        CitizenConsentDTO result = mapper.citizenConsentMapper(citizenConsent);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(citizenConsent.getChannelState(), result.getChannelState());
            assertEquals(citizenConsent.getHashedFiscalCode(), result.getHashedFiscalCode());
            assertEquals(citizenConsent.getUserId(), result.getUserId());
            assertEquals(citizenConsent.getCreationDate(), result.getCreationDate());
            assertEquals(citizenConsent.getLastUpdateDate(), result.getLastUpdateDate());
            assertEquals(citizenConsent.getChannelId(), result.getChannelId());
        });
    }
}
