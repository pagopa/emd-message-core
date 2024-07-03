package it.gov.pagopa.message.core.repository;




import it.gov.pagopa.message.core.model.Channel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChannelRepository extends MongoRepository<Channel,String> {

    List<Channel> findByIdInAndStateTrue(List<String> channelIds);

}
