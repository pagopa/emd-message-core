package it.gov.pagopa.message.core.connector.tpp;



import it.gov.pagopa.message.core.model.Channel;

import java.util.List;

public interface TppConnector {
    List<Channel> getChannelsList(List<String> channelIds);
}
