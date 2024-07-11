package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.connector.tpp.TppConnectorImpl;
import it.gov.pagopa.message.core.dto.*;
import it.gov.pagopa.message.core.enums.OutcomeStatus;
import it.gov.pagopa.common.utils.Utils;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.CitizenConsent;
import it.gov.pagopa.message.core.dto.Outcome;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Slf4j
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

        log.info("[EMD][SEND-MESSAGE] Recived message: {}",messageDTO);
        String hashedFiscalCode = Utils.createSHA256(messageDTO.getRecipientId());
        ArrayList<CitizenConsent> citizenConsentList =
                citizenService.getCitizenConsentsEnabled(hashedFiscalCode);

        if(citizenConsentList.isEmpty()) {
            log.info("[EMD][SEND-MESSAGE] Citizen consent list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }

        log.info("[EMD][SEND-MESSAGE] Citizen consent list: {}",citizenConsentList);
        List<Channel> channelList = tppService.getChannelsList(
                                citizenConsentList
                                .stream()
                                .map(CitizenConsent::getChannelId)
                                .toList()
                        );

        if(channelList.isEmpty()) {
            log.info("[EMD][SEND-MESSAGE] Channel list is empty");
            return new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED);
        }
        log.info("[EMD][SEND-MESSAGE] Channel list: {}",channelList);

        for (CitizenConsent citizenConsent : citizenConsentList) {
            Iterator<Channel> iterator = channelList.iterator();
            while (iterator.hasNext()) {
                Channel channel = iterator.next();
                if (channel.getId().equals(citizenConsent.getChannelId())) {
                    log.info("[EMD][SEND-MESSAGE] Channel: {}",channel.getBusinessName());
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
