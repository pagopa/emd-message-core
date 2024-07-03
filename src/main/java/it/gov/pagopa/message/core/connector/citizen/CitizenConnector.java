package it.gov.pagopa.message.core.connector.citizen;

import it.gov.pagopa.message.core.model.CitizenConsent;

import java.util.ArrayList;

public interface CitizenConnector {
    ArrayList<CitizenConsent> getCitizenConsentsEnabled(String fiscalCode);


}
