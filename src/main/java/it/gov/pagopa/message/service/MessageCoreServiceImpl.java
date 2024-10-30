package it.gov.pagopa.message.service;

import it.gov.pagopa.common.utils.CommonUtilities;
import it.gov.pagopa.message.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;



@Slf4j
@Service
public class MessageCoreServiceImpl implements MessageCoreService {

    private final BloomFilterServiceImpl bloomFilterServiceImpl;

    private final MessageProducerServiceImpl messageErrorProducerService;

    public MessageCoreServiceImpl(BloomFilterServiceImpl bloomFilterServiceImpl,
                                  MessageProducerServiceImpl messageProducerService) {
        this.bloomFilterServiceImpl = bloomFilterServiceImpl;
        this.messageErrorProducerService = messageProducerService;
    }


    @Override
    public Mono<Boolean> sendMessage(MessageDTO messageDTO) {
        log.info("[EMD-MESSAGE-CORE][SEND]Received message: {}", messageDTO);
        if (bloomFilterServiceImpl.mightContain(CommonUtilities.createSHA256(messageDTO.getRecipientId()))) {
            messageErrorProducerService.enqueueMessage(messageDTO);
            return Mono.just(true);
        }
        else
            return Mono.just(false);
    }

}
