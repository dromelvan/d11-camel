package org.d11.camel.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

import org.d11.api.model.whoscored.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class WhoScoredMatchParser extends JSoupJavaScriptParser<WhoScoredMatch, WhoScoredMatchJavaScriptVariables> {
    
    public WhoScoredMatchParser() {
        super(new WhoScoredMatchJavaScriptVariables());
    }

    @Override
    public WhoScoredMatch parseDocument(Document document) {
        WhoScoredMatch whoScoredMatch = null;

        try {
            WhoScoredMatchJavaScriptVariables whoScoredMatchJavaScriptVariables = getJavaScriptVariables(document);
            whoScoredMatch = new WhoScoredMatch(whoScoredMatchJavaScriptVariables);
            if(whoScoredMatch.getElapsed() != null && whoScoredMatch.getElapsed().equals("FT")) {
                // Check that the game is really finished.
                Elements elements = document.getElementsByClass("finished");
                if(elements.isEmpty()) {
                    whoScoredMatch.setElapsed("90");
                }
            }
        } catch (NullPointerException e) {
            Pattern matchIdPattern = Pattern.compile(".*ws_matchID = '(\\d*)'.*", Pattern.DOTALL);
            Pattern matchHeaderPattern = Pattern.compile(".*matchHeader.load\\(\\[(\\d*),(\\d*),'(.*)','(.*)','(.*)','.*',\\d*,,,,,,.*", Pattern.DOTALL);

            whoScoredMatch = new WhoScoredMatch();

            for (Element element : document.getElementsByTag("script")) {
                Matcher matchIdMatcher = matchIdPattern.matcher(element.toString());
                if (matchIdMatcher.matches()) {
                    whoScoredMatch.setWhoScoredId(Integer.parseInt(matchIdMatcher.group(1)));
                }
                Matcher matchHeaderMatcher = matchHeaderPattern.matcher(element.toString());
                if (matchHeaderMatcher.matches()) {
                    whoScoredMatch.setHomeTeam(new WhoScoredTeam(Integer.parseInt(matchHeaderMatcher.group(1)), matchHeaderMatcher.group(3)));
                    whoScoredMatch.setAwayTeam(new WhoScoredTeam(Integer.parseInt(matchHeaderMatcher.group(2)), matchHeaderMatcher.group(4)));

                    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(matchHeaderMatcher.group(5), dateTimeFormat).plusHours(2);

                    whoScoredMatch.setDatetime(dateTime.format(WhoScoredMatch.dateTimeFormatter));
                    whoScoredMatch.setElapsed("NA");
                }
            }
        }

        // This can happen for example when a match is postponed
        if(whoScoredMatch.getHomeTeam() == null || whoScoredMatch.getAwayTeam() == null) {
            whoScoredMatch.setHomeTeam(new WhoScoredTeam(0, "Unknown"));
            whoScoredMatch.setAwayTeam(new WhoScoredTeam(0, "Unknown"));            
            whoScoredMatch.setElapsed("NA");
        }
        
        return whoScoredMatch;
    }
}
