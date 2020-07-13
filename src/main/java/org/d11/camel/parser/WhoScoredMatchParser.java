package org.d11.camel.parser;

import org.d11.camel.rest.Match;

public class WhoScoredMatchParser extends D11CamelParser<Match> {

    @Override
    public Match parse(String html) {
        Match match = new Match();
        match.setId(30);
        match.setMatchDayNumber(33);
        match.setSeasonName("2019-2020");
        match.setStatus(0);
        match.setWhoscoredId("12212");
        match.setDatetime("2019-01-12 17:00:00");
        return match;
    }

}
