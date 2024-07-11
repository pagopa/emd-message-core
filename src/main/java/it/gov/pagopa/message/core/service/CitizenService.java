package it.gov.pagopa.message.core.service;


import it.gov.pagopa.message.core.dto.CitizenConsentDTO;

public interface CitizenService {

    CitizenConsentDTO createCitizenConsent(CitizenConsentDTO citizenConsent);
    CitizenConsentDTO deleteCitizenConsent(String fiscalCode, String channelId);
    CitizenConsentDTO updateCitizenConsent(String fiscalCode, String channelId);

}
