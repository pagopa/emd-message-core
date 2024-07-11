package it.gov.pagopa.message.core.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.message.core.dto.ChannelDTO;
import it.gov.pagopa.message.core.faker.ChannelDTOFaker;
import it.gov.pagopa.message.core.service.ChannelServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(ChannelControllerImpl.class)
class ChannelControllerTest {

    @MockBean
    private ChannelServiceImpl channelService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveChannel_Ok() throws Exception {
        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);

        Mockito.when(channelService.createChannel(channelDTO)).thenReturn(channelDTO);

        MvcResult result = mockMvc.perform(
                post("/emd/onboarding-tpp/channel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channelDTO))
        ).andExpect(status().isOk()).andReturn();

        ChannelDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ChannelDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(channelDTO, resultResponse);
    }

    @Test
    void updateChannel_Ok() throws Exception {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);

        Mockito.when(channelService.updateChannel(channelDTO.getId())).thenReturn(channelDTO);

        MvcResult result = mockMvc.perform(
                put("/emd/onboarding-tpp/channel/{channelId}", channelDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channelDTO))
        ).andExpect(status().isOk()).andReturn();

        ChannelDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ChannelDTO.class);

        Assertions.assertNotNull(resultResponse);
    }

    @Test
    void deleteChannel_Ok() throws Exception {

        ChannelDTO channelDTO = ChannelDTOFaker.mockInstance(true);

        Mockito.when(channelService.deleteChannel(channelDTO.getId())).thenReturn(channelDTO);

        MvcResult result = mockMvc.perform(
                delete("/emd/onboarding-tpp/channel/{channelId}", channelDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channelDTO))
        ).andExpect(status().isOk()).andReturn();

        ChannelDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ChannelDTO.class);

        Assertions.assertNotNull(resultResponse);
    }


}
