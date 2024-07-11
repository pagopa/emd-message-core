package it.gov.pagopa.message.core.connector.citizen;


import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.repository.CitizenRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CitizenConnectorImpl implements CitizenConnector {

    private final CitizenRepository citizenRepository;

    public CitizenConnectorImpl(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Override
    public ArrayList<CitizenConsent> getCitizenConsentsEnabled(String fiscalCode) {
        return citizenRepository.findByHashedFiscalCodeAndChannelStateTrue(fiscalCode);
    }
}
