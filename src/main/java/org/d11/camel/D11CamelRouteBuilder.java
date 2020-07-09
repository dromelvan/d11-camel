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
                    
        // Wait for a match id to appear on the update match datetimes queue.         
        from("activemq:queue:" + this.activeMQProperties.getUpdateMatchDatetimesQueue())
            .routeId("DownloadUpdateMatchDatetimesRoute")
            // Throttle the route to avoid triggering Whoscored flood protection.
            .throttle(1).timePeriodMillis(10000)
            // Get the match from the D11 api, construct the destination file path from its properties and set the Whoscored match url as body.
            .doTry()
                .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatch().getEndpoint().replace(":id", "${body}"))
                .unmarshal().json(JsonLibrary.Jackson, MatchResponse.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Match match = exchange.getMessage().getBody(MatchResponse.class).getMatch();
                        String destinationDirectory = String.format(whoscoredProperties.getMatchDestinationDirectoryDatetimes(), match.getSeasonName(), match.getMatchDayNumber());
                        exchange.setProperty("destinationDirectory", destinationDirectory);
                        exchange.setProperty("tempDirectory", whoscoredProperties.getMatchTempDirectory());
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
                        String fileName = String.format("%s.html", document.title().replace("/", "-"));
                        exchange.setProperty("fileName", fileName);
                    }                
                })
                .log("Writing file ${exchangeProperty.destinationDirectory}/${exchangeProperty.fileName}")
                .toD("file://${exchangeProperty.destinationDirectory}?fileName=${exchangeProperty.fileName}&tempPrefix=${exchangeProperty.tempDirectory}")
            .doCatch(Exception.class)
                .setBody(exceptionMessage())
                .log("Could not download match file: ${body}")
            .end();
    }
 
}
