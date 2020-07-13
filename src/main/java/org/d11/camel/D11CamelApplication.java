package org.d11.camel;

import org.d11.camel.event.LoginFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.*;

@SpringBootApplication
public class D11CamelApplication implements ApplicationListener<LoginFailedEvent> {

    @Autowired 
    private ConfigurableApplicationContext configurableApplicationContext;
    
    public static void main(String[] args) {
        SpringApplication.run(D11CamelApplication.class, args);
    }
    
    public D11CamelApplication(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
    }

    @Override
    public void onApplicationEvent(LoginFailedEvent event) {
        // Shut down the application if we failed to log in to the D11 api.
        this.configurableApplicationContext.close();
    }
    
}
