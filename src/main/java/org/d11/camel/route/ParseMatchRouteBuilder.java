package org.d11.camel.route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.api.model.Match;
import org.d11.camel.parser.WhoScoredMatchParser;
import org.d11.camel.properties.WhoScoredProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParseMatchRouteBuilder extends RouteBuilder {

    private final static Pattern matchFileNamePattern = Pattern.compile("(\\d{4}-\\d{4})/(\\d{2})/(\\d{4}).*");
    private WhoScoredProperties whoScoredProperties;
    
    @Autowired
    public ParseMatchRouteBuilder(WhoScoredProperties whoScoredProperties) {
        this.whoScoredProperties = whoScoredProperties;
    }
    
    @Override
    public void configure() throws Exception {    
        from("file://" + this.whoScoredProperties.getMatchDownloadDirectory() + "?recursive=true&delete=true")
        .routeId("ParseMatchRoute")
        .log(LoggingLevel.DEBUG, "Parsing file ${body.fileName}.")
        .to("file://" + this.whoScoredProperties.getMatchDataDirectory())
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
                    if(match.getDatetime() == null) {
                        // This will postpone the match.
                        match.setDatetime("");
                    }
                    exchange.getMessage().setBody(match);   
                }
            }                
        })
        .setProperty("fileName", simple("${body.seasonName}/${body.matchDayNumber}/${body.homeTeam.name} vs ${body.awayTeam.name} (${body.elapsed})"))
        .marshal().json(JsonLibrary.Jackson, Match.class, true)
        .to("file://" + this.whoScoredProperties.getMatchUploadDirectory() + "?fileName=${exchangeProperty.fileName}.json");
    }
    
}
