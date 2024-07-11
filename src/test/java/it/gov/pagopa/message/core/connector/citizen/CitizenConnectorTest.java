package it.gov.pagopa.message.core.connector.citizen;

import it.gov.pagopa.message.core.faker.CitizenConsentFaker;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.repository.CitizenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        CitizenConnectorImpl.class
})
class CitizenConnectorTest {

    @Autowired
    CitizenConnectorImpl citizenConnector;

    @MockBean
    CitizenRepository citizenRepository;
    @Test
    void getCitizenConsentsEnabled_Ok(){
        CitizenConsent citizenConsent = CitizenConsentFaker.mockInstance(true);
        ArrayList<CitizenConsent> citizenConsents = new ArrayList<>(List.of(citizenConsent));
        Mockito.when(citizenRepository.findByHashedFiscalCodeAndChannelStateTrue(citizenConsent.getHashedFiscalCode()))
                .thenReturn(citizenConsents);
        ArrayList<CitizenConsent> response = citizenConnector.getCitizenConsentsEnabled(citizenConsent.getHashedFiscalCode());
        assertEquals(citizenConsents,response);
    }
}
