package it.gov.pagopa.common.mongo;

import it.gov.pagopa.common.configuration.CustomReactiveMongoHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoHealthConfig {
    @Bean
    public CustomReactiveMongoHealthIndicator customMongoHealthIndicator(ReactiveMongoTemplate reactiveMongoTemplate) {
        return new CustomReactiveMongoHealthIndicator(reactiveMongoTemplate);
    }
}

