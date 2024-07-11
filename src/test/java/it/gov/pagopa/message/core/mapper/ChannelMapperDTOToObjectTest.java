package it.gov.pagopa.message.core.mapper;

import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.faker.ChannelDTOFaker;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.model.mapper.ChannelMapperDTOToObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ChannelMapperDTOToObjectTest {

    private ChannelMapperDTOToObject mapper;
    @BeforeEach
    void setUp() {
        mapper = new ChannelMapperDTOToObject();
    }

    @Test
    void transactionMapperTest() {
        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);
        Channel result = mapper.channelDTOMapper(channelDTO);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(channelDTO.getState(), result.getState());
            assertEquals(channelDTO.getUri(), result.getUri());
            assertEquals(channelDTO.getMessageUrl(), result.getMessageUrl());
            assertEquals(channelDTO.getAuthenticationType(), result.getAuthenticationType());
            assertEquals(channelDTO.getAuthenticationUrl(), result.getAuthenticationUrl());
            assertEquals(channelDTO.getId(), result.getId());
            assertEquals(channelDTO.getBusinessName(), result.getBusinessName());
            assertEquals(channelDTO.getContact(), result.getContact());
            assertEquals(channelDTO.getEntityId(), result.getEntityId());

        });
    }

}
