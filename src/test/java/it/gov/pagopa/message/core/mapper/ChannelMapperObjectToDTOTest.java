package it.gov.pagopa.message.core.mapper;


import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.dto.mapper.ChannelMapperObjectToDTO;
import it.gov.pagopa.message.core.faker.ChannelFaker;
import it.gov.pagopa.message.core.model.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

@Service
class ChannelMapperObjectToDTOTest {

    private ChannelMapperObjectToDTO mapper;
    @BeforeEach
    void setUp() {
        mapper = new ChannelMapperObjectToDTO();
    }

    @Test
    void transactionMapperTest() {
        Channel channel = ChannelFaker.mockInstance(true);
        ChannelDTO result = mapper.channelMapper(channel);

        assertAll(() -> {
            assertNotNull(result);
            assertEquals(channel.getState(), result.getState());
            assertEquals(channel.getUri(), result.getUri());
            assertEquals(channel.getMessageUrl(), result.getMessageUrl());
            assertEquals(channel.getAuthenticationType(), result.getAuthenticationType());
            assertEquals(channel.getAuthenticationUrl(), result.getAuthenticationUrl());
            assertEquals(channel.getId(), result.getId());
            assertEquals(channel.getBusinessName(), result.getBusinessName());
            assertEquals(channel.getContact(), result.getContact());
            assertEquals(channel.getEntityId(), result.getEntityId());

        });
    }
}
