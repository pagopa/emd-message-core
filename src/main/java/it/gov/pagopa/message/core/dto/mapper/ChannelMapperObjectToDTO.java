package it.gov.pagopa.message.core.dto.mapper;



import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.model.Channel;
import org.springframework.stereotype.Service;

@Service
public class ChannelMapperObjectToDTO {

    public ChannelDTO channelMapper(Channel channel){
        return ChannelDTO.builder()
                .state(channel.getState())
                .uri(channel.getUri())
                .messageUrl(channel.getMessageUrl())
                .authenticationUrl(channel.getAuthenticationUrl())
                .authenticationType(channel.getAuthenticationType())
                .id(channel.getId())
                .businessName(channel.getBusinessName())
                .contact(channel.getContact())
                .entityId(channel.getEntityId())
                .lastUpdateDate(channel.getLastUpdateDate())
                .creationDate(channel.getCreationDate())
                .build();
    }
}
