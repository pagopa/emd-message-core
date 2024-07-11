package it.gov.pagopa.message.core.faker;


import it.gov.pagopa.message.core.enums.AuthenticationType;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.Contact;

public class ChannelFaker {

    private ChannelFaker(){}

    public static Channel mockInstance(Boolean bias) {
        return Channel.builder()
                .id("channelId")
                .uri("uri")
                .messageUrl("messageUrl")
                .authenticationUrl("AuthenticationUrl")
                .authenticationType(AuthenticationType.OAUTH2)
                .state(bias)
                .entityId("entityId")
                .businessName("businessName")
                .contact(new Contact("name","number", "email"))
                .build();
    }
}
