package org.d11.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.camel.properties.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateMatchDatetimesRouteBuilder extends RouteBuilder {

    private ActiveMQProperties activeMQProperties;
    private D11ApiProperties d11ApiProperties;
    
    @Autowired
    public UpdateMatchDatetimesRouteBuilder(ActiveMQProperties activeMQProperties, D11ApiProperties d11ApiProperties) {
        this.activeMQProperties = activeMQProperties;
        this.d11ApiProperties = d11ApiProperties;    
    }
    
    @Override
    public void configure() throws Exception {
        // Wait for a match day id (or 'current'/'upcoming) to appear on the ActiveMQ queue.
        from("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesRequestQueue())
            .routeId("PollUpdateMatchDatetimesRequestsRoute")
            // The http component seems to assume we want to POST the message from the activemq component.
            // We have to set the method to GET.
            .setHeader("CamelHttpMethod", constant("GET"))
            .doTry()
                // Get the requested match day from the api.
                .unmarshal().json(JsonLibrary.Jackson)
                .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatchDay().getEndpoint().replace(":id", "${body}"))
            .doCatch(Exception.class)
                .setBody(exceptionMessage())
                .log("Could not find match day: ${body}")
                .stop()
            .end()
                // Get the match id list from the match day and split it.            
                .split(jsonpath(this.d11ApiProperties.getMatchDay().getMatchIdsJsonPath()))
                    .convertBodyTo(String.class)
                    // Put each match id on the update match datetimes queue.
                    .to("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesQueue())
                .end();            
    }

}
