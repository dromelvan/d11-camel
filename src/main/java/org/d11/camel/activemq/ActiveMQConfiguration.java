package org.d11.camel.activemq;

import org.springframework.context.annotation.*;
import org.springframework.jms.support.converter.*;

@Configuration
public class ActiveMQConfiguration {

    /**
     * Use a Jackson converter to convert messages.
     * 
     * @return The message converter we want to use.
     */
    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    
}
