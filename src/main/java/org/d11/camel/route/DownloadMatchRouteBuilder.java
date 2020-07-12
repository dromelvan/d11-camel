package org.d11.camel.route;

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
public class DownloadMatchRouteBuilder extends RouteBuilder {
        
    private ActiveMQProperties activeMQProperties;
    private D11ApiProperties d11ApiProperties;
    private WhoscoredProperties whoscoredProperties;
    
    @Autowired
    public DownloadMatchRouteBuilder(ActiveMQProperties activeMQProperties, D11ApiProperties d11ApiProperties, WhoscoredProperties whoscoredProperties) {
        this.activeMQProperties = activeMQProperties;
        this.d11ApiProperties = d11ApiProperties;    
        this.whoscoredProperties = whoscoredProperties;        
    }
    
    @Override
    public void configure() {                            
        // Wait for a match id to appear on the update match datetimes queue.         
        from("activemq:queue:" + this.activeMQProperties.getDownloadMatchQueue())
            .routeId("DownloadMatchRoute")
            // Throttle the route to avoid triggering Whoscored flood protection.
            .throttle(1).timePeriodMillis(10000)
            // Get the match from the D11 api and set the Whoscored match url as body.
            .doTry()
                .setProperty("matchId", simple("${body}"))
                .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatch().getEndpoint().replace(":id", "${body}"))
                .unmarshal().json(JsonLibrary.Jackson, MatchResponse.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Match match = exchange.getMessage().getBody(MatchResponse.class).getMatch();
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
                        String fileName = String.format("%s %s.html", exchange.getProperty("matchId"), document.title().replace("/", "-"));                        
                        exchange.setProperty("fileName", fileName);
                    }                
                })
                .log("Writing file " + whoscoredProperties.getMatchDestinationDirectory() + "/${exchangeProperty.fileName}")
                .toD("file://" + whoscoredProperties.getMatchDestinationDirectory() + "?fileName=${exchangeProperty.fileName}&tempPrefix=" + whoscoredProperties.getMatchTempDirectory())
            .doCatch(Exception.class)
                .setBody(exceptionMessage())
                .log("Could not download match file: ${body}")
            .end();
    }
 
}