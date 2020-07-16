package org.d11.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.d11.camel.properties.D11ApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateMatchDatetimeRouteBuilder extends RouteBuilder {

    private D11ApiProperties d11ApiProperties;
    
    @Autowired
    public UpdateMatchDatetimeRouteBuilder(D11ApiProperties d11ApiProperties) {
        this.d11ApiProperties = d11ApiProperties;
    }
    
    @Override
    public void configure() throws Exception {
        from("direct:update-match-datetime")
            .routeId("UpdateMatchdatetimeRoute")
            .to("direct:login")
            .log("${exchangeProperty.authenticationToken}")
            .log("${body}")
            .log("TODO: Update match datetime route.");
    }

}
