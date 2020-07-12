package org.d11.camel.scheduler;

import org.d11.camel.properties.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.*;

@Configuration
@EnableScheduling
public class SchedulerConfiguration {
    
    private ActiveMQProperties activeMQProperties;
    private JmsTemplate jmsTemplate;

    @Autowired
    public SchedulerConfiguration(ActiveMQProperties activeMQProperties, JmsTemplate jmsTemplate) {
        this.activeMQProperties = activeMQProperties;
        this.jmsTemplate = jmsTemplate;
    }
    
    @Scheduled(cron = "0,15,30,45 * * * * ?")
    public void testSchedule() {
        this.jmsTemplate.convertAndSend(this.activeMQProperties.getDownloadMatchDayQueue(), MatchDayEndpoint.CURRENT);
        //this.jmsTemplate.convertAndSend(this.activeMQProperties.getUpdateMatchDatetimesRequestQueue(), "559");
    }
    
}
