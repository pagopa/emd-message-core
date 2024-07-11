package it.gov.pagopa.message.core.faker;


import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.enums.AuthenticationType;
import it.gov.pagopa.message.core.model.Contact;

import java.time.LocalDateTime;

public class ChannelDTOFaker {

    private ChannelDTOFaker(){}

    public static ChannelDTO mockInstance(Boolean bias) {
        return ChannelDTO.builder()
                .id("channelId")
                .uri("uri")
                .messageUrl("messageUrl")
                .authenticationUrl("AuthenticationUrl")
                .authenticationType(AuthenticationType.OAUTH2)
                .state(bias)
                .entityId("entityId")
                .businessName("businessName")
                .contact(new Contact("name","number", "email"))
                .lastUpdateDate(LocalDateTime.now())
                .creationDate(LocalDateTime.now())
                .build();
    }
}
