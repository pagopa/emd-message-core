package it.gov.pagopa.message.core.model.mapper;

import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.model.Channel;
import org.springframework.stereotype.Service;

@Service
public class ChannelMapperDTOToObject {

    public Channel channelDTOMapper(ChannelDTO channelDTO){
        return Channel.builder()
                .state(true)
                .uri(channelDTO.getUri())
                .messageUrl(channelDTO.getMessageUrl())
                .authenticationUrl(channelDTO.getAuthenticationUrl())
                .authenticationType(channelDTO.getAuthenticationType())
                .id(channelDTO.getId())
                .businessName(channelDTO.getBusinessName())
                .contact(channelDTO.getContact())
                .entityId(channelDTO.getEntityId())
                .build();
    }
}
