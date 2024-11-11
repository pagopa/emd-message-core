package it.gov.pagopa.message.utils;

import it.gov.pagopa.message.dto.MessageDTO;
import it.gov.pagopa.message.utils.faker.MessageDTOFaker;
import it.gov.pagopa.message.utils.faker.QueueMessageFaker;
import org.springframework.messaging.Message;

public class TestUtils {

    public TestUtils(){}

    public static final String RESPONSE = "OK";
    public static final MessageDTO MESSAGE_DTO = MessageDTOFaker.mockInstance();
    public static final String MESSAGE_URL = "messageUrl";
    public static final String AUTHENTICATION_URL = "authenticationUrl";
    public static final long RETRY = 1;
    public static final String FISCAL_CODE = MESSAGE_DTO.getRecipientId();
    public static Message<MessageDTO> QUEUE_MESSAGE = QueueMessageFaker.mockInstance(MESSAGE_DTO);
}
