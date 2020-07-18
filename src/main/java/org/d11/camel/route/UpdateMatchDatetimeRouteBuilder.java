package org.d11.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.d11.api.model.MatchResponse;
import org.d11.camel.properties.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateMatchDatetimeRouteBuilder extends RouteBuilder {

    private KeyProperties keyProperties;
    private D11ApiProperties d11ApiProperties;
    private WhoScoredProperties whoScoredProperties;
    
    @Autowired
    public UpdateMatchDatetimeRouteBuilder(KeyProperties keyProperties, D11ApiProperties d11ApiProperties, WhoScoredProperties whoScoredProperties) {
        this.keyProperties = keyProperties;
        this.d11ApiProperties = d11ApiProperties;
        this.whoScoredProperties = whoScoredProperties;
    }
    
    @Override
    public void configure() throws Exception {
        from("file://" + this.whoScoredProperties.getMatchUploadDirectory() + "?recursive=true&delete=true")
            .routeId("UpdateMatchdatetimeRoute")            
            .to("direct:login")
            .setProperty("matchId", jsonpath(this.d11ApiProperties.getMatch().getIdJsonPath()))
            .setProperty("datetime", jsonpath(this.d11ApiProperties.getMatch().getDatetimeJsonPath()))
            .to("file://" + this.whoScoredProperties.getParsedMatchDataDirectory())
            .setHeader("CamelHttpMethod", constant("PUT"))
            .toD("http://" + this.d11ApiProperties.getBaseUrl() + this.d11ApiProperties.getMatchDateTime().getEndpoint()
                    .replace(":id", "${exchangeProperty.matchId}")
                    .replace(":datetime", "${exchangeProperty.datetime}")
                    .replace(":user", this.keyProperties.getD11ApiUser())
                    .replace(":authentication_token", "${exchangeProperty.authenticationToken}"))
            .unmarshal().json(JsonLibrary.Jackson, MatchResponse.class, true)
            .choice()
                .when().simple("${body.match.datetime} == '" + this.d11ApiProperties.getPostponedDatetime() + "'")
                    .log("Postponed match ${body.match.id}")
                .otherwise()
                    .log("Moved match ${body.match.id} to ${body.match.datetime}")
            .end();            
    }

}
