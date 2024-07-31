package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.connector.tpp.TppConnectorImpl;
import it.gov.pagopa.message.core.dto.*;
import it.gov.pagopa.message.core.enums.OutcomeStatus;
import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.dto.Outcome;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static it.gov.pagopa.common.utils.Utils.logInfo;


@Service
public class MessageCoreServiceImpl implements MessageCoreService {

    private final CitizenConnectorImpl citizenService;
    private final TppConnectorImpl tppService;
    private final SendMessageServiceImpl sendMessageServiceImpl;

    public MessageCoreServiceImpl(CitizenConnectorImpl citizenService,
                                  TppConnectorImpl tppService,
                                  SendMessageServiceImpl sendMessageServiceImpl
                                        ) {
        this.tppService = tppService;
        this.citizenService = citizenService;
        this.sendMessageServiceImpl = sendMessageServiceImpl;
    }

    @Override
    public Outcome sendMessage(MessageDTO messageDTO) {

        logInfo("[EMD][SEND-MESSAGE] Recived message: %s".formatted(messageDTO));
        String hashedFiscalCode = Utils.createSHA256(messageDTO.getRecipientId());
        ArrayList<CitizenConsent> citizenConsentList =
                citizenService.getCitizenConsentsEnabled(hashedFiscalCode);

        if(citizenConsentList.isEmpty()) {
            logInfo("[EMD][SEND-MESSAGE] Citizen consent list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }

        logInfo("[EMD][SEND-MESSAGE] Citizen consent list: %s".formatted(citizenConsentList));
        List<Channel> channelList = tppService.getChannelsList(
                                citizenConsentList
                                .stream()
                                .map(CitizenConsent::getChannelId)
                                .toList()
                        );

        if(channelList.isEmpty()) {
            logInfo("[EMD][SEND-MESSAGE] Channel list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }
        logInfo("[EMD][SEND-MESSAGE] Channel list: %s".formatted(channelList));

        for (CitizenConsent citizenConsent : citizenConsentList) {
            Iterator<Channel> iterator = channelList.iterator();
            while (iterator.hasNext()) {
                Channel channel = iterator.next();
                if (channel.getId().equals(citizenConsent.getChannelId())) {
                    logInfo("[EMD][SEND-MESSAGE] Channel: %s".formatted(channel.getBusinessName()));
                    sendMessageServiceImpl.sendMessage(
                            messageDTO,
                            channel.getMessageUrl(),
                            channel.getAuthenticationUrl()
                    );
                    iterator.remove();
                    break;
                }
            }
        }

        return new Outcome(OutcomeStatus.OK);
    }


}
