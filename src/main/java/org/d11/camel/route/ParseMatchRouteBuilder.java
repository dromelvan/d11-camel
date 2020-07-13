package org.d11.camel.route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.camel.parser.WhoScoredMatchParser;
import org.d11.camel.properties.WhoscoredProperties;
import org.d11.camel.rest.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParseMatchRouteBuilder extends RouteBuilder {

    private final static Pattern matchFileNamePattern = Pattern.compile("(\\d{4}-\\d{4})/(\\d{2})/(\\d{4}).*");
    private WhoscoredProperties whoscoredProperties;
    
    @Autowired
    public ParseMatchRouteBuilder(WhoscoredProperties whoscoredProperties) {
        this.whoscoredProperties = whoscoredProperties;
    }
    
    @Override
    public void configure() throws Exception {
        from("file://" + this.whoscoredProperties.getMatchDownloadDirectory() + "?recursive=true&delete=true")
            .routeId("ParseMatchRoute")
            .log("Parsing file ${body.fileName}")
            .to("file://" + this.whoscoredProperties.getMatchDataDirectory())
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    // In the new API we'll update match datetime with whoscoredId but until then we'll need to keep the D11 matchId in the file name.
                    Matcher matcher = matchFileNamePattern.matcher(exchange.getMessage().getBody(GenericFile.class).getFileName());
                    if(matcher.matches()) {
                        String seasonName = matcher.group(1);
                        int matchDayNumber = Integer.parseInt(matcher.group(2));
                        int matchId = Integer.parseInt(matcher.group(3));
                        
                        Match match = new WhoScoredMatchParser().parse(exchange.getMessage().getBody(String.class));
                        match.setId(matchId);
                        match.setSeasonName(seasonName);
                        match.setMatchDayNumber(matchDayNumber);
                        exchange.getMessage().setBody(match);   
                    }
                }                
            })
            .choice()
                .when(simple("${body} !is 'org.d11.camel.rest.Match'"))
                    // If we end up here, the filename regex didn't match which means the filename was invalid.
                    .log("Invalid filename ${body.fileName}.")
                    .stop()
                .when(simple("${body.status} == 0"))
                    // The match is still pending so we'll update datetime only.
                    .log("Updating datetime for match ${body.id}")
                    .to("direct:update-match-datetime")
                .otherwise()
                    // The match is active or finished so we'll update match stats.
                    .log("Updating match stats for match ${body.id}.")
                    .to("direct:update-match-stats")
            .end()
            .setProperty("fileName", simple("${body.seasonName}/${body.matchDayNumber}/${body.id}"))
            .marshal().json(JsonLibrary.Jackson, Match.class, true)
            .to("file://data/d11/matches?fileName=${exchangeProperty.fileName}.json");
    }

}
