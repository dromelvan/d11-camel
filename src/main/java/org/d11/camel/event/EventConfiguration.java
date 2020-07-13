package org.d11.camel.event;

import org.springframework.context.annotation.*;
import org.springframework.context.event.*;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * This configuration ensures events published by ApplicationEventPublisher will be handled asynchronously.
 */
@Configuration
public class EventConfiguration {
    
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();        
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }
    
}