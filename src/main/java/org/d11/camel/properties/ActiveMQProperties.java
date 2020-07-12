package org.d11.camel.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import lombok.Data;

@Data
@Configuration
@PropertySource("classpath:activemq.properties")
@ConfigurationProperties(prefix = "d11.activemq.queue")
public class ActiveMQProperties {

    private String downloadMatchDayQueue;
    private String downloadMatchQueue;
    
}
