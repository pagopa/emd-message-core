package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.dto.mapper.ChannelMapperObjectToDTO;
import it.gov.pagopa.message.core.exception.custom.TppNotOnboardedException;
import it.gov.pagopa.message.core.faker.ChannelDTOFaker;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.mapper.ChannelMapperDTOToObject;
import it.gov.pagopa.message.core.repository.ChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        ChannelServiceImpl.class,
        ChannelMapperObjectToDTO.class,
        ChannelMapperDTOToObject.class
})
class ChannelServiceTest {
    @Autowired
    ChannelServiceImpl channelService;
    @MockBean
    ChannelRepository channelRepository;
    @Autowired
    ChannelMapperObjectToDTO mapperToDTO;
    @Autowired
    ChannelMapperDTOToObject mapperToObject;



    @Test
    void createChannel_Ok() {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);
        Channel channel = mapperToObject.channelDTOMapper(channelDTO);

        Mockito.when(channelRepository.save(Mockito.any()))
                .thenReturn(channel);

        ChannelDTO response = channelService.createChannel(channelDTO);

        assertEquals(channelDTO, response);
    }

    @Test
    void deleteChannel_Ok() {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(false);
        Channel channel = mapperToObject.channelDTOMapper(channelDTO);

        Mockito.when(channelRepository.findById(channelDTO.getId()))
                .thenReturn(Optional.ofNullable(channel));

        ChannelDTO result = channelService.deleteChannel(channelDTO.getId());
        channelDTO.setLastUpdateDate(result.getLastUpdateDate());
        assertEquals(result,channelDTO);
    }

    @Test
    void deleteChannel_Ko_TppNotOnboarded() {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(false);

        Mockito.when(channelRepository.findById(channelDTO.getId()))
                .thenReturn(Optional.empty());

        Executable executable = () -> channelService.deleteChannel(channelDTO.getId());
        TppNotOnboardedException exception = assertThrows(TppNotOnboardedException.class, executable);

        assertEquals("Tpp not onboarded", exception.getMessage());
    }

    @Test
    void updateChannel_Ok() {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);
        Channel channel = mapperToObject.channelDTOMapper(channelDTO);

        Mockito.when(channelRepository.findById(channelDTO.getId()))
                .thenReturn(Optional.ofNullable(channel));

        ChannelDTO result = channelService.updateChannel(channelDTO.getId());
        channelDTO.setLastUpdateDate(result.getLastUpdateDate());
        assertEquals(result,channelDTO);
    }

    @Test
    void updateChannel_Ko_TppNotOnboarded() {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);

        Mockito.when(channelRepository.findById(channelDTO.getId()))
                .thenReturn(Optional.empty());

        Executable executable = () -> channelService.updateChannel(channelDTO.getId());
        TppNotOnboardedException exception = assertThrows(TppNotOnboardedException.class, executable);

        assertEquals("Tpp not onboarded", exception.getMessage());
    }

}
