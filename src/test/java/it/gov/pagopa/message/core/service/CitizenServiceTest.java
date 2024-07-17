package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.dto.mapper.CitizenConsentMapperObjectToDTO;
import it.gov.pagopa.message.core.exception.custom.EmdEncryptionException;
import it.gov.pagopa.message.core.exception.custom.UserNotOnboardedException;
import it.gov.pagopa.message.core.faker.CitizenConsentDTOFaker;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.model.mapper.CitizenConsentMapperDTOToObject;
import it.gov.pagopa.message.core.repository.CitizenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        CitizenServiceImpl.class,
        CitizenConsentMapperObjectToDTO.class,
        CitizenConsentMapperDTOToObject.class,
        CitizenConnectorImpl.class
})
class CitizenServiceTest {
    @Autowired
    CitizenServiceImpl citizenService;

    @MockBean
    CitizenRepository citizenRepository;
    @Autowired
    CitizenConsentMapperObjectToDTO mapperToDTO;
    @Autowired
    CitizenConsentMapperDTOToObject mapperToObject;

    @Test
    void createCitizenConsent_Ok(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);
        CitizenConsent citizenConsent = mapperToObject.citizenConsentDTOMapper(citizenConsentDTO);
        String hashedFiscalCode = Utils.createSHA256(citizenConsentDTO.getHashedFiscalCode());
        citizenConsentDTO.setHashedFiscalCode(hashedFiscalCode);
        citizenConsent.setHashedFiscalCode(hashedFiscalCode);
        Mockito.when(citizenRepository.save(any()))
                .thenReturn(citizenConsent);
        CitizenConsentDTO response = citizenService.createCitizenConsent(citizenConsentDTO);
        assertEquals(citizenConsentDTO,response);
    }

    @Test
    void createCitizenConsent_Ko_EmdEncryptError(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);

        try (MockedStatic<Utils> mockedStatic = Mockito.mockStatic(Utils.class)) {
            mockedStatic.when(() -> Utils.createSHA256(any()))
                    .thenThrow(EmdEncryptionException.class);

            assertThrows(EmdEncryptionException.class, () -> citizenService.createCitizenConsent(citizenConsentDTO));

        }
    }

    @Test
    void deleteCitizenConsent_Ok(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(false);
        CitizenConsent citizenConsent = mapperToObject.citizenConsentDTOMapper(citizenConsentDTO);
        Mockito.when(citizenRepository
                    .findByHashedFiscalCodeAndChannelId(Utils.createSHA256(citizenConsentDTO.getHashedFiscalCode()),citizenConsentDTO.getChannelId()))
                        .thenReturn(citizenConsent);
        CitizenConsentDTO result =  citizenService.deleteCitizenConsent(citizenConsentDTO.getHashedFiscalCode(),citizenConsentDTO.getChannelId());
        citizenConsentDTO.setLastUpdateDate(result.getLastUpdateDate());
        citizenConsentDTO.setCreationDate(result.getCreationDate());
        assertEquals(result,citizenConsentDTO);
    }

    @Test
    void deleteCitizenConsent_Ko_UserNotOnboarded(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(false);
        String hashedFiscalCode = citizenConsentDTO.getHashedFiscalCode();
        String channelId = citizenConsentDTO.getChannelId();
        Mockito.when(citizenRepository
                    .findByHashedFiscalCodeAndChannelId(Utils.createSHA256(citizenConsentDTO.getHashedFiscalCode()),citizenConsentDTO.getChannelId()))
                        .thenReturn(null);

        assertThrows(UserNotOnboardedException.class, () ->
                citizenService.deleteCitizenConsent(hashedFiscalCode,channelId));
    }

    @Test
    void updateCitizenConsent_Ok(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);
        CitizenConsent citizenConsent = mapperToObject.citizenConsentDTOMapper(citizenConsentDTO);
        Mockito.when(citizenRepository
                        .findByHashedFiscalCodeAndChannelId(Utils.createSHA256(citizenConsentDTO.getHashedFiscalCode()),citizenConsentDTO.getChannelId()))
                .thenReturn(citizenConsent);
        CitizenConsentDTO result =  citizenService.updateCitizenConsent(citizenConsentDTO.getHashedFiscalCode(),citizenConsentDTO.getChannelId());
        citizenConsentDTO.setLastUpdateDate(result.getLastUpdateDate());
        assertEquals(result,citizenConsentDTO);
    }

    @Test
    void updateCitizenConsent_Ko_UserNotOnboarded() {

        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);

        String hashedFiscalCode = citizenConsentDTO.getHashedFiscalCode();
        String channelId = citizenConsentDTO.getChannelId();

        Mockito.when(citizenRepository.findByHashedFiscalCodeAndChannelId(hashedFiscalCode, channelId))
                .thenReturn(null);

        assertThrows(UserNotOnboardedException.class, () -> {
            citizenService.updateCitizenConsent(hashedFiscalCode, channelId);
        });
    }

    @Test
    void citizenConsent_Ko_EmdEncryptError(){
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);
        String hashedFiscalCode = citizenConsentDTO.getHashedFiscalCode();
        String channelId = citizenConsentDTO.getChannelId();

        try (MockedStatic<Utils> mockedStatic = Mockito.mockStatic(Utils.class)) {
            mockedStatic.when(() -> Utils.createSHA256(any()))
                    .thenThrow(EmdEncryptionException.class);

           assertThrows(EmdEncryptionException.class, () ->
                    citizenService.deleteCitizenConsent(hashedFiscalCode, channelId));
        }
    }
}
