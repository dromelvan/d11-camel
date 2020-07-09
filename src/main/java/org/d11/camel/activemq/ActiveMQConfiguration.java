package org.d11.camel.activemq;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.*;

@Configuration
public class ActiveMQConfiguration {

    /**
     * Use a Jackson converter to convert messages.
     * TODO: At the moment it seems we might want to send Strings only within the application
     * We're leaving this here so we know what to do (uncomment the @Bean annotation) in case
     * that changes.
     * 
     * @return The message converter we want to use.
     */
    //@Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    
}
