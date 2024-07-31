package it.gov.pagopa.message.core.service;

import it.gov.pagopa.message.core.dto.MessageDTO;
import it.gov.pagopa.message.core.dto.TokenDTO;
import it.gov.pagopa.message.core.faker.MessageDTOFaker;
import it.gov.pagopa.message.core.faker.TokenDTOFaker;
import it.gov.pagopa.message.core.stub.model.MessageMapperDTOToObject;
import it.gov.pagopa.message.core.stub.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        SendMessageServiceImpl.class
})
class SendMessageServiceTest {

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    MessageErrorProducerService errorProducerService;

    @MockBean
    MessageRepository messageRepository;
    @MockBean
    MessageMapperDTOToObject mapperDTOToObject;

    @Autowired
    SendMessageServiceImpl sendMessageService;

    @Test
    void sendMessage1_Ok(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        TokenDTO tokenDTO = TokenDTOFaker.mockInstance();
        ResponseEntity<TokenDTO> tokenReponse = new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        ResponseEntity<String> messageReponse = new ResponseEntity<>("OK", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenReturn(tokenReponse);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(messegaUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenReturn(messageReponse);

        assertDoesNotThrow(() ->sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl));

    }

    @Test
    void sendMessage1_Error_Token(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";

        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenThrow(new RestClientException("Mocked exception"));

        Mockito.doNothing().when(errorProducerService).sendError(messegeDTO,messegaUrl,authenticationUrl);
        sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl);
        Mockito.verify( errorProducerService, times(1)).sendError(messegeDTO,messegaUrl,authenticationUrl);
    }

    @Test
    void sendMessage1_Error_Message(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        TokenDTO tokenDTO = TokenDTOFaker.mockInstance();
        ResponseEntity<TokenDTO> tokenReponse = new ResponseEntity<>(tokenDTO, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenReturn(tokenReponse);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(messegaUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenThrow(new RestClientException("Mocked exception"));

        Mockito.doNothing().when(errorProducerService).sendError(messegeDTO,messegaUrl,authenticationUrl);
        sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl);
        Mockito.verify( errorProducerService, times(1)).sendError(messegeDTO,messegaUrl,authenticationUrl);

    }

    @Test
    void sendMessage2_Ok(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        long retry = 1;
        TokenDTO tokenDTO = TokenDTOFaker.mockInstance();
        ResponseEntity<TokenDTO> tokenReponse = new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        ResponseEntity<String> messageReponse = new ResponseEntity<>("OK", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenReturn(tokenReponse);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(messegaUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenReturn(messageReponse);

        assertDoesNotThrow(() ->sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl,retry));

    }
    @Test
    void sendMessage2_Error_Token(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        long retry = 1;
        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenThrow(new RestClientException("Mocked exception"));

        Mockito.doNothing().when(errorProducerService).sendError(messegeDTO,messegaUrl,authenticationUrl,retry);
        sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl,retry);
        Mockito.verify( errorProducerService, times(1)).sendError(messegeDTO,messegaUrl,authenticationUrl,retry);
    }
    @Test
    void sendMessage2_Error_Message(){
        MessageDTO messegeDTO = MessageDTOFaker.mockInstance();
        String messegaUrl = "messegaUrl";
        String authenticationUrl = "authenticationUrl";
        long retry = 1;
        TokenDTO tokenDTO = TokenDTOFaker.mockInstance();
        ResponseEntity<TokenDTO> tokenReponse = new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
                Mockito.eq(authenticationUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(TokenDTO.class)
        )).thenReturn(tokenReponse);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(messegaUrl),
                Mockito.eq(HttpMethod.POST),
                any(HttpEntity.class),
                Mockito.eq(String.class)
        )).thenThrow(new RestClientException("Mocked exception"));

        Mockito.doNothing().when(errorProducerService).sendError(messegeDTO,messegaUrl,authenticationUrl,retry);
        sendMessageService.sendMessage(messegeDTO,messegaUrl,authenticationUrl,retry);
        Mockito.verify( errorProducerService, times(1)).sendError(messegeDTO,messegaUrl,authenticationUrl,retry);

    }
}
