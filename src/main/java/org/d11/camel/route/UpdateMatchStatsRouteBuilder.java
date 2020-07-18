package org.d11.camel.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.d11.camel.properties.D11ApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateMatchStatsRouteBuilder extends RouteBuilder {

    private D11ApiProperties d11ApiProperties;
    
    @Autowired
    public UpdateMatchStatsRouteBuilder(D11ApiProperties d11ApiProperties) {
        this.d11ApiProperties = d11ApiProperties;
    }
    
    @Override
    public void configure() throws Exception {
        from("direct:update-match-stats")
            .routeId("UpdateMatchStatsRoute")
            .to("direct:login")
            .log(LoggingLevel.INFO, "TODO: Update match stats route.");
    }

}
