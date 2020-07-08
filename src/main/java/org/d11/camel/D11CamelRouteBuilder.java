package org.d11.camel;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.camel.properties.*;
import org.d11.camel.rest.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class D11CamelRouteBuilder extends RouteBuilder {
        
    private ActiveMQProperties activeMQProperties;
    private D11ApiProperties d11ApiProperties;
    private WhoscoredProperties whoscoredProperties;
    
    @Autowired
    public D11CamelRouteBuilder(ActiveMQProperties activeMQProperties, D11ApiProperties d11ApiProperties, WhoscoredProperties whoscoredProperties) {
        this.activeMQProperties = activeMQProperties;
        this.d11ApiProperties = d11ApiProperties;    
        this.whoscoredProperties = whoscoredProperties;        
    }
    
    @Override
    public void configure() {        
        // Wait for a request to update match datetimes appears on the ActiveMQ queue.
        from("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesRequestQueue())
            // The http component seems to assume we want to POST the message from the activemq component.
            // We have to set the method to GET since we're only using ActiveMQ to trigger this route.
            .setHeader("CamelHttpMethod", constant("GET"))
            .to("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getCurrentMatchDay().getEndpoint())
            // Get the match id list from the match day and split it.            
            .split(jsonpath(this.d11ApiProperties.getCurrentMatchDay().getMatchIdsJsonPath()))
                .convertBodyTo(String.class)
                // Put each match id on the update match datetimes queue.
                .to(("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesQueue()));
        
        
        // Wait for a match id to appear on the update match datetimes queue.         
        from("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesQueue())
            // Throttle the route to avoid triggering Whoscored flood protection.
            .throttle(1).timePeriodMillis(10000)
            // Get the match from the D11 api, construct the destination file path from its properties and set the Whoscored match url as body.
            .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatch().getEndpoint().replace(":id", "${body}"))
            .unmarshal().json(JsonLibrary.Jackson, MatchResponse.class)
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    Match match = exchange.getMessage().getBody(MatchResponse.class).getMatch();
                    String fileDestination = String.format("download/whoscored/%s/%d", match.getSeasonName(), match.getMatchDayNumber());
                    exchange.setProperty("fileDestination", fileDestination);     
                    exchange.getIn().setBody(whoscoredProperties.getMatchUrl().replace(":id", match.getWhoscoredId()));
                }                
            })            
            // Download the file with a Selenium downloader and move it to the destination directory.
            .log("Downloading ${body}")
            .toD("selenium:${body}")
            // Use Jsoup to set the filename to the title of the html document.
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    Document document = Jsoup.parse(exchange.getMessage().getBody(String.class));
                    String fileDestination = exchange.getProperty("fileDestination", String.class);
                    fileDestination += String.format("?fileName=%s.html", document.title().replace("/", "-"));
                    exchange.setProperty("fileDestination", fileDestination);
                }                
            })
            .log("Writing file ${exchangeProperty.fileDestination}")
            .toD("file://${exchangeProperty.fileDestination}");        
    }
 
}
