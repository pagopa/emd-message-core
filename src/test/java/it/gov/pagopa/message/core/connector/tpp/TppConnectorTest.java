package it.gov.pagopa.message.core.connector.tpp;

import it.gov.pagopa.message.core.faker.ChannelFaker;
import it.gov.pagopa.message.core.model.Channel;
import it.gov.pagopa.message.core.repository.ChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
        TppConnectorImpl.class
})
class TppConnectorTest {

    @Autowired
    TppConnectorImpl tppConnector;

    @MockBean
    ChannelRepository channelRepository;
    @Test
    void getChannelsList_Ok() {

        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("1", "2", "3"));
        List<Channel> channelList = List.of(ChannelFaker.mockInstance(true));



        Mockito.when(channelRepository.findByIdInAndStateTrue(arrayList))
                .thenReturn(channelList);

        List<Channel> response = tppConnector.getChannelsList(arrayList);

        assertEquals(channelList, response);

    }
}
