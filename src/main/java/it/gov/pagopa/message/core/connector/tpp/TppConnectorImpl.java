package it.gov.pagopa.message.core.connector.tpp;

import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TppConnectorImpl implements TppConnector {

    private final ChannelRepository channelRepository;

    public TppConnectorImpl(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public  List<Channel> getChannelsList(List<String> channelIds){
        return channelRepository.findByIdInAndStateTrue(channelIds);
    }


}
