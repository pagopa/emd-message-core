package it.gov.pagopa.message.core.service;

import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.connector.tpp.TppConnectorImpl;
import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.Outcome;
import it.gov.pagopa.message.core.exception.custom.EmdEncryptionException;
import it.gov.pagopa.message.core.faker.ChannelFaker;
import it.gov.pagopa.message.core.faker.CitizenConsentFaker;
import it.gov.pagopa.message.core.faker.MessageDTOFaker;
import it.gov.pagopa.message.core.faker.OutcomeFaker;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.CitizenConsent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        MessageCoreServiceImpl.class
})
class MessageCoreServiceTest {

    @MockBean
    CitizenConnectorImpl citizenService;
    @MockBean
    TppConnectorImpl tppService;

    @MockBean
    SendMessageServiceImpl sendMessageService;

    @Autowired
    MessageCoreServiceImpl messageCoreService;

    @Test
    void sendMessage_Ok()  {
        MessageDTO message = MessageDTOFaker.mockInstance();
        ArrayList<CitizenConsent> citizenConsents = new ArrayList<>(List.of(CitizenConsentFaker.mockInstance(true)));
        List<Channel> channelDTOS = new ArrayList<>(List.of(ChannelFaker.mockInstance(true)));
        Outcome outcome = OutcomeFaker.mockInstance(true);

        Mockito.when(citizenService.getCitizenConsentsEnabled(any()))
                .thenReturn(citizenConsents);

        Mockito.when(tppService.getChannelsList(any()))
                .thenReturn(channelDTOS);

        Mockito.doNothing().when(sendMessageService).sendMessage(any(),any(),any());
        Outcome serviceResponse = messageCoreService.sendMessage(message);
        assertEquals(outcome,serviceResponse);

    }

    @Test
    void sendMessage_NoChannelEnabled_Case_NoConsents()  {
        MessageDTO message = MessageDTOFaker.mockInstance();
        ArrayList<CitizenConsent> citizenConsents = new ArrayList<>();

        Outcome outcome = OutcomeFaker.mockInstance(false);

        Mockito.when(citizenService.getCitizenConsentsEnabled(any()))
                .thenReturn(citizenConsents);


        Outcome serviceResponse = messageCoreService.sendMessage(message);
        assertEquals(outcome,serviceResponse);

    }

    @Test
    void sendMessage_NoChannelEnabled_Case_NoChannels()  {
        MessageDTO message = MessageDTOFaker.mockInstance();
        ArrayList<CitizenConsent> citizenConsents = new ArrayList<>(List.of(CitizenConsentFaker.mockInstance(true)));
        List<Channel> channelS = List.of();
        Outcome outcome = OutcomeFaker.mockInstance(false);

        Mockito.when(citizenService.getCitizenConsentsEnabled(any()))
                .thenReturn(citizenConsents);

        Mockito.when(tppService.getChannelsList(any()))
                .thenReturn(channelS);

        Outcome serviceResponse = messageCoreService.sendMessage(message);
        assertEquals(outcome,serviceResponse);

    }

    @Test
    void sendMessage_Ko_EmdEncryptError()  {

        MessageDTO message = MessageDTOFaker.mockInstance();

        try (MockedStatic<Utils> mockedStatic = Mockito.mockStatic(Utils.class)) {
            mockedStatic.when(() -> Utils.createSHA256(any()))
                    .thenThrow(EmdEncryptionException.class);

            assertThrows(EmdEncryptionException.class, () -> messageCoreService.sendMessage(message));
        }
    }

}
