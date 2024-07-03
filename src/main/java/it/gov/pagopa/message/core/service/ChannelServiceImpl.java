package it.gov.pagopa.message.core.service;



import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.dto.mapper.ChannelMapperObjectToDTO;
import it.gov.pagopa.message.core.exception.custom.TppNotOnboardedException;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.mapper.ChannelMapperDTOToObject;
import it.gov.pagopa.message.core.repository.ChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
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
        log.info("[EMD][CREATE-CHANNEL] Received message: {}",channelDTO.toString());
        Channel channel = mapperToObject.channelDTOMapper(channelDTO);
        channel  = channelRepository.save(channel);
        log.info("[EMD][CREATE-CHANNEL] Created");
        return mapperToDTO.channelMapper(channel);
    }


    @Override
    public ChannelDTO deleteChannel(String channelId) {
        log.info("[EMD][DELETE-CHANNEL] Received  channelId {} ", channelId);
        Optional<Channel> optionalChannel = channelRepository.findById(channelId);
        if (optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();
            channel.setState(false);
            channelRepository.save(channel);
            log.info("[EMD][CREATE-CHANNEL] Deleted");
            return mapperToDTO.channelMapper(channel);
        } else {
            log.error("[EMD][DELETE-CHANNEL] Tpp not onboarded");
            throw new TppNotOnboardedException("Tpp not onboarded", true, null);
        }
    }

    @Override
    public ChannelDTO updateChannel(String channelId) {
        log.info("[EMD][UPDATE-CHANNEL] Received channelId {} ", channelId);
        Optional<Channel> optionalChannel = channelRepository.findById(channelId);
        if (optionalChannel.isPresent()) {
            Channel channel = optionalChannel.get();
            channel.setState(true);
            channelRepository.save(channel);
            log.info("[EMD][CREATE-CHANNEL] Updated");
            return mapperToDTO.channelMapper(channel);
        } else {
            log.error("[EMD][UPDATE-CHANNEL] Tpp not onboarded");
            throw new TppNotOnboardedException("Tpp not onboarded", true, null);
        }

    }

}
