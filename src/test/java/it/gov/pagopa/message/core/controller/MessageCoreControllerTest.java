package it.gov.pagopa.message.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.Outcome;
import it.gov.pagopa.message.core.faker.MessageDTOFaker;
import it.gov.pagopa.message.core.faker.OutcomeFaker;
import it.gov.pagopa.message.core.service.MessageCoreServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageCoreControllerImpl.class)
class MessageCoreControllerTest {


    @MockBean
    private MessageCoreServiceImpl messageCoreService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendMessage_Ok() throws Exception {

        MessageDTO message = MessageDTOFaker.mockInstance();
        Outcome outcome = OutcomeFaker.mockInstance(true);

        Mockito.when(messageCoreService.sendMessage(message)).thenReturn(outcome);

        MvcResult result = mockMvc.perform(
                post("/emd/message-core/sendMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message))
        ).andExpect(status().isOk()).andReturn();

        Outcome resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Outcome.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(outcome,resultResponse);
    }

    @Test
    void sendMessage_NoChannelEnabled() throws Exception{

        MessageDTO message = MessageDTOFaker.mockInstance();
        Outcome outcome = OutcomeFaker.mockInstance(false);

        Mockito.when(messageCoreService.sendMessage(message)).thenReturn(outcome);

        MvcResult result = mockMvc.perform(
                post("/emd/message-core/sendMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message))
        ).andExpect(status().isAccepted()).andReturn();

        Outcome resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Outcome.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(outcome,resultResponse);
    }

}
