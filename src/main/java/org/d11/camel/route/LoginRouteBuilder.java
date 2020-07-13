package org.d11.camel.route;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.camel.event.LoginFailedEvent;
import org.d11.camel.properties.*;
import org.d11.camel.rest.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LoginRouteBuilder extends RouteBuilder {
    
    private ApplicationEventPublisher applicationEventPublisher;    
    private D11ApiProperties d11ApiProperties;
    private KeyProperties keyProperties;
    
    @Autowired
    public LoginRouteBuilder(ApplicationEventPublisher applicationEventPublisher, D11ApiProperties d11ApiProperties, KeyProperties keyProperties) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.d11ApiProperties = d11ApiProperties;
        this.keyProperties = keyProperties;
    } 

    @Override
    public void configure() throws Exception {
        from("direct:login")
            .doTry()
                .setProperty("pre-login-body", simple("${body}"))
                .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getLogin().getEndpoint()
                                                                               .replace(":user", this.keyProperties.getD11ApiUser())
                                                                               .replace(":password", this.keyProperties.getD11ApiPassword()))
                .unmarshal().json(JsonLibrary.Jackson, LoginResponse.class)
                .setProperty("authenticationToken", simple("${body.authenticationToken}"))
                .setBody(simple("${exchangeProperty.pre-login-body}"))
                .log("Login successful.")
            .doCatch(Exception.class)
                .log("Login failed.")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        applicationEventPublisher.publishEvent(new LoginFailedEvent(this));
                    }                    
                })
            .end();                
    }
    
}
