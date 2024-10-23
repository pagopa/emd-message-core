package it.gov.pagopa.message.service;

import it.gov.pagopa.message.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.connector.tpp.TppConnectorImpl;
import it.gov.pagopa.message.dto.CitizenConsentDTO;
import it.gov.pagopa.message.dto.TppDTO;
import it.gov.pagopa.message.dto.TppIdList;
import it.gov.pagopa.message.enums.OutcomeStatus;
import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.model.Outcome;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.List;



@Slf4j
@Service
public class MessageCoreServiceImpl implements MessageCoreService {


    private final CitizenConnectorImpl citizenConnector;
    private final TppConnectorImpl tppConnector;
    private final SendMessageServiceImpl sendMessageService;

    public MessageCoreServiceImpl(CitizenConnectorImpl citizenConnector,
                                  TppConnectorImpl tppConnector,
                                  SendMessageServiceImpl sendMessageService) {
        this.tppConnector = tppConnector;
        this.citizenConnector = citizenConnector;
        this.sendMessageService = sendMessageService;
    }


    @Override
    public Mono<Outcome> sendMessage(MessageDTO messageDTO) {
        log.info("[EMD-MESSAGE-CORE][SEND]Received message: {}", messageDTO);

        return citizenConnector.getCitizenConsentsEnabled(messageDTO.getRecipientId())
                .flatMap(citizenConsentDTOSList -> {
                    if (citizenConsentDTOSList.isEmpty()) {
                        log.info("[EMD-MESSAGE-CORE][SEND]Citizen consent list is empty");
                        return Mono.just(new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED));
                    }

                    log.info("[EMD-MESSAGE-CORE][SEND]Citizen consent list: {}", citizenConsentDTOSList);

                    List<String> tppIds = citizenConsentDTOSList.stream()
                            .map(CitizenConsentDTO::getTppId)
                            .toList();

                    return tppConnector.getTppsEnabled(new TppIdList(tppIds))
                            .flatMap(tppList -> {
                                if (tppList.isEmpty()) {
                                    log.info("[EMD-MESSAGE-CORE][SEND]Channel list is empty");
                                    return Mono.just(new Outcome(OutcomeStatus.NO_CHANNELS_ENABLED));
                                }
                                log.info("[EMD-MESSAGE-CORE][SEND]Channel list: {}", tppList);
                                return processMessages(tppList, messageDTO);
                            });
                });
    }
    private Mono<Outcome> processMessages(List<TppDTO> tppDTOList,
                                          MessageDTO messageDTO) {
        Flux.fromIterable(tppDTOList)
             .doOnNext(tppDTO -> {
                            log.info("[EMD-MESSAGE-CORE][SEND]Prepare sending message to: {}", tppDTO.getTppId());
                            sendMessageService.sendMessage(messageDTO, tppDTO.getMessageUrl(), tppDTO.getAuthenticationUrl(), tppDTO.getEntityId());
                        })
            .subscribe();

        return Mono.just(new Outcome(OutcomeStatus.OK));
    }
}
