package it.gov.pagopa.message.core.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.message.core.connector.citizen.CitizenConnectorImpl;
import it.gov.pagopa.message.core.dto.CitizenConsentDTO;
import it.gov.pagopa.message.core.faker.CitizenConsentDTOFaker;
import it.gov.pagopa.message.core.model.mapper.CitizenConsentMapperDTOToObject;
import it.gov.pagopa.message.core.service.CitizenServiceImpl;
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

@WebMvcTest(CitizenControllerImpl.class)
class CitizenControllerTest {

    @MockBean
    private CitizenServiceImpl citizenService;

    @MockBean
    private CitizenConnectorImpl citizenConnector;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    CitizenConsentMapperDTOToObject mapperToObject;

    @Test
    void saveCitizenConsent_Ok() throws Exception {
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);

        Mockito.when(citizenService.createCitizenConsent(citizenConsentDTO)).thenReturn(citizenConsentDTO);

        MvcResult result = mockMvc.perform(
                post("/emd/onboarding-citizen/citizenConsents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(citizenConsentDTO))
        ).andExpect(status().isOk()).andReturn();

        CitizenConsentDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CitizenConsentDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(citizenConsentDTO,resultResponse);
    }

    @Test
    void updateCitizenConsent_Ok() throws Exception {
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(true);

        Mockito.when(
                citizenService.updateCitizenConsent(citizenConsentDTO.getHashedFiscalCode(),citizenConsentDTO.getChannelId()))
                .thenReturn(citizenConsentDTO);

        MvcResult result = mockMvc.perform(
                    put("/emd/onboarding-citizen/citizenConsents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(citizenConsentDTO))
        ).andExpect(status().isOk()).andReturn();

        CitizenConsentDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CitizenConsentDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(citizenConsentDTO,resultResponse);
    }


    @Test
    void deleteCitizenConsent_Ok() throws Exception {
        String userId = "testUserId";
        String channelId = "testChannelId";
        CitizenConsentDTO citizenConsentDTO = CitizenConsentDTOFaker.mockInstance(false);
        Mockito.when(
            citizenService.deleteCitizenConsent(userId,channelId))
                    .thenReturn(citizenConsentDTO);

        MvcResult result = mockMvc.perform(
                delete("/emd/onboarding-citizen/citizenConsents")
                        .param("userId", userId)
                        .param("channelId", channelId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        CitizenConsentDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CitizenConsentDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(citizenConsentDTO,resultResponse);
    }

}
