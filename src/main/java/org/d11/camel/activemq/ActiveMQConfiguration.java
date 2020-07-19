package org.d11.camel.activemq;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.*;

@Configuration
public class ActiveMQConfiguration {

    // Value from application.properties
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;
    private Logger logger = LoggerFactory.getLogger(ActiveMQConfiguration.class);
    
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

    /**
     * Listen for messages from external sources.
     * 
     * @return BrokerService with a connector for external sources to add messages to the embedded ActiveMz
     * @throws Exception
     */
    @Bean
    public BrokerService broker() throws Exception {
        BrokerService brokerService = new BrokerService();
        brokerService.addConnector(this.brokerUrl);
        // For this application we gain little by having persistence in the broker except for problems
        // when we manage to put something bad on the queue.
        brokerService.setPersistent(false);
        return brokerService;
    }

    @JmsListener(destination = "testQueue")
    public void listen(String message) {
        logger.info("TestQueue message: {}.", message);
    }
    
}
