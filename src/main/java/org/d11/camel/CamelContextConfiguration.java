package org.d11.camel;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelContextConfiguration {

    @Autowired
    public CamelContextConfiguration(CamelContext camelContext) {
        // If we don't do this we can only read from streams (like the http camel) once.
        // When we do this we can do that however may times we want without converting to string first.
        camelContext.setStreamCaching(true);
    }
    
}
