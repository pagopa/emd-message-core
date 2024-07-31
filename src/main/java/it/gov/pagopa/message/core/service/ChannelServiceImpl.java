package it.gov.pagopa.message.core.service;



import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.dto.mapper.ChannelMapperObjectToDTO;
import it.gov.pagopa.message.core.exception.custom.TppNotOnboardedException;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.mapper.ChannelMapperDTOToObject;
import it.gov.pagopa.message.core.repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static it.gov.pagopa.common.utils.Utils.logInfo;

@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapperObjectToDTO mapperToDTO;
    private final ChannelMapperDTOToObject mapperToObject;

    public ChannelServiceImpl(ChannelRepository channelRepository, ChannelMapperObjectToDTO mapperToDTO, ChannelMapperDTOToObject mapperToObject) {
        this.channelRepository = channelRepository;
        this.mapperToDTO = mapperToDTO;
        this.mapperToObject = mapperToObject;
    }


    @Override
    public ChannelDTO createChannel(ChannelDTO channelDTO) {
        logInfo("[EMD][CREATE-CHANNEL] Received message: %s".formatted(channelDTO.toString()));
        Channel channel = mapperToObject.channelDTOMapper(channelDTO);
        channel.setCreationDate(LocalDateTime.now());
        channel.setLastUpdateDate(LocalDateTime.now());
        channel  = channelRepository.save(channel);
        logInfo("[EMD][CREATE-CHANNEL] Created");
        return mapperToDTO.channelMapper(channel);
    }


    @Override
    public ChannelDTO deleteChannel(String channelId) {
        logInfo("[EMD][DELETE-CHANNEL] Received  channelId %s ".formatted(channelId));
        Optional<Channel> optionalChannel = channelRepository.findById(channelId);
        if (optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();
            channel.setState(false);
            channel.setLastUpdateDate(LocalDateTime.now());
            channelRepository.save(channel);
            logInfo("[EMD][CREATE-CHANNEL] Deleted");
            return mapperToDTO.channelMapper(channel);
        } else {
            logInfo("[EMD][DELETE-CHANNEL] Tpp not onboarded");
            throw new TppNotOnboardedException("Tpp not onboarded", true, null);
        }
    }

    @Override
    public ChannelDTO updateChannel(String channelId) {
        logInfo("[EMD][UPDATE-CHANNEL] Received channelId %s ".formatted(channelId));
        Optional<Channel> optionalChannel = channelRepository.findById(channelId);
        if (optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();
            channel.setState(true);
            channel.setLastUpdateDate(LocalDateTime.now());
            channelRepository.save(channel);
            logInfo("[EMD][CREATE-CHANNEL] Updated");
            return mapperToDTO.channelMapper(channel);
        } else {
            logInfo("[EMD][UPDATE-CHANNEL] Tpp not onboarded");
            throw new TppNotOnboardedException("Tpp not onboarded", true, null);
        }

    }

}
