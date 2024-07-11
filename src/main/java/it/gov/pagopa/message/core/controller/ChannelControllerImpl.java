package it.gov.pagopa.message.core.controller;


import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.service.ChannelServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ChannelControllerImpl implements ChannelController {

    private final ChannelServiceImpl channelService;

    public ChannelControllerImpl(ChannelServiceImpl channelService) {
        this.channelService = channelService;
    }

    @Override
    public ResponseEntity<ChannelDTO> deleteChannel(String channelId) {
        ChannelDTO channelDTO= channelService.deleteChannel(channelId);
        return new ResponseEntity<>(channelDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChannelDTO> updateChannel(String channelId) {
        ChannelDTO channelDTO= channelService.updateChannel(channelId);
        return new ResponseEntity<>(channelDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChannelDTO> saveChannel(ChannelDTO channelDTO) {
        channelDTO= channelService.createChannel(channelDTO);
        return new ResponseEntity<>(channelDTO, HttpStatus.OK);
    }
}
