package it.gov.pagopa.message.core.service;



import it.gov.pagopa.message.core.dto.ChannelDTO;



public interface ChannelService {

    ChannelDTO  createChannel(ChannelDTO channelDTO);
    ChannelDTO  deleteChannel(String channelId);
    ChannelDTO  updateChannel(String channelId);

}
