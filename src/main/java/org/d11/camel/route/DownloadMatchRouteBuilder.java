package org.d11.camel.route;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.api.model.*;
import org.d11.camel.properties.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DownloadMatchRouteBuilder extends RouteBuilder {
        
    private ActiveMQProperties activeMQProperties;
    private D11ApiProperties d11ApiProperties;
    private WhoScoredProperties whoScoredProperties;
    
    @Autowired
    public DownloadMatchRouteBuilder(ActiveMQProperties activeMQProperties, D11ApiProperties d11ApiProperties, WhoScoredProperties whoScoredProperties) {
        this.activeMQProperties = activeMQProperties;
        this.d11ApiProperties = d11ApiProperties;    
        this.whoScoredProperties = whoScoredProperties;        
    }
    
    @Override
    public void configure() {                            
        // Wait for a match id to appear on the update match datetimes queue.         
        from("activemq:queue:" + this.activeMQProperties.getDownloadMatchQueue())
            .routeId("DownloadMatchRoute")
            // Throttle the route to avoid triggering Whoscored flood protection.
            .throttle(1).timePeriodMillis(10000)
            // Get the match from the D11 api, construct the download file path from its properties and set the Whoscored match url as body.
            .doTry()
                .setProperty("matchId", simple("${body}"))
                .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatch().getEndpoint().replace(":id", "${body}"))
                .unmarshal().json(JsonLibrary.Jackson, MatchResponse.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Match match = exchange.getMessage().getBody(MatchResponse.class).getMatch();
                        String destinationDirectory = String.format(whoScoredProperties.getMatchDownloadDirectory() + "/%s/%s", match.getSeasonName(), match.getMatchDayNumber());
                        exchange.setProperty("destinationDirectory", destinationDirectory);
                        exchange.setProperty("tempDirectory", whoScoredProperties.getMatchTempDirectory());
                        exchange.getIn().setBody(whoScoredProperties.getMatchUrl().replace(":id", String.valueOf(match.getWhoScoredId())));
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
                .log("Writing file ${exchangeProperty.destinationDirectory}/${exchangeProperty.fileName}")
                .toD("file://${exchangeProperty.destinationDirectory}?fileName=${exchangeProperty.fileName}&tempPrefix=${exchangeProperty.tempDirectory}")
            .doCatch(Exception.class)
                .setBody(exceptionMessage())
                .log("Could not download match file: ${body}")
            .end();
    }
 
}
