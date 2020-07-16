package org.d11.api.model.whoscored;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

import org.d11.api.model.*;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;
import org.slf4j.*;

public class WhoScoredMatch extends Match {

    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final static Logger logger = LoggerFactory.getLogger(WhoScoredMatch.class);
    
    public WhoScoredMatch() {}
    
    @SuppressWarnings("unchecked")
    public WhoScoredMatch(WhoScoredMatchJavaScriptVariables match) {
        setWhoScoredId((Integer) match.get(WhoScoredMatchJavaScriptVariables.MATCH_ID));

        Map<?,?> matchCentreData = (Map<?,?>) match.get(WhoScoredMatchJavaScriptVariables.MATCH_CENTRE_DATA);
        Pattern startTimePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{1,2}:\\d{1,2}).*");
        Matcher startTimeMatcher = startTimePattern.matcher((String) matchCentreData.get(WhoScoredMatchJavaScriptVariables.START_TIME));
        if (startTimeMatcher.matches()) {
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(startTimeMatcher.group(1), dateTimeFormat);
            setDatetime(dateTime.plusHours(2).format(WhoScoredMatch.dateTimeFormatter));
        } else {
            logger.warn("Could not parse start time from input {}.", matchCentreData.get(WhoScoredMatchJavaScriptVariables.START_TIME));
        }

        String elapsed = (String) matchCentreData.get(WhoScoredMatchJavaScriptVariables.ELAPSED);
        if(elapsed != null) {
            if(elapsed.endsWith("'")) {
                elapsed = elapsed.replace("'", "");
            } else if(elapsed.trim().isEmpty()) {
                elapsed = "NA";
            }
        }
        setElapsed(elapsed);
        if(elapsed.equals("NA")) {
            setStatus(0);
        } else if(elapsed.equals("FT")) {
            setStatus(2);
        } else {
            setStatus(1);
        }

        Map<String, Object> homeTeamMap = (Map<String, Object>) matchCentreData.get(WhoScoredMatchJavaScriptVariables.HOME_TEAM);
        Map<String, Object> awayTeamMap = (Map<String, Object>) matchCentreData.get(WhoScoredMatchJavaScriptVariables.AWAY_TEAM);
        setHomeTeam(parseTeam(homeTeamMap));
        setAwayTeam(parseTeam(awayTeamMap));

        int homeTeamGoals = 0;
        int awayTeamGoals = 0;
        for(Goal goal : getGoals()) {
            if(goal.getOwnGoal()) {
                goal.setTeam(goal.getTeam() == getHomeTeam() ? getAwayTeam() : getHomeTeam());
            }
            if(goal.getTeam() == getHomeTeam()) {
                homeTeamGoals++;
            } else {
                awayTeamGoals++;
            }
        }

        for (PlayerMatchStat playerMatchStat : getPlayerMatchStats()) {
            if (playerMatchStat.getTeam() == getHomeTeam()) {
                playerMatchStat.setGoalsConceded(awayTeamGoals);
            } else {
                playerMatchStat.setGoalsConceded(homeTeamGoals);
            }
        }        
    }
    
    private Team parseTeam(Map<String, Object> teamMap) {
        Team team = new WhoScoredTeam(teamMap);
        Map<Integer, PlayerMatchStat> playerMap = new HashMap<Integer, PlayerMatchStat>();

        int maxRating = 0;
        List<PlayerMatchStat> moms = new ArrayList<PlayerMatchStat>();

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> playerMatchStatMaps = (List<Map<String,Object>>) teamMap.get(WhoScoredMatchJavaScriptVariables.TEAM_PLAYERS);
        for(Map<String,Object> playerMatchStatMap : playerMatchStatMaps) {
            WhoScoredPlayerMatchStat playerMatchStat = new WhoScoredPlayerMatchStat(playerMatchStatMap);
            playerMatchStat.setTeam(team);
            getPlayerMatchStats().add(playerMatchStat);
            playerMap.put(playerMatchStat.getPlayer().getWhoScoredId(), playerMatchStat);

            if (playerMatchStat.getRating() > maxRating) {
                maxRating = playerMatchStat.getRating();
                moms.clear();
                moms.add(playerMatchStat);
            } else if (playerMatchStat.getRating() == maxRating) {
                moms.add(playerMatchStat);
            }
        }

        for (PlayerMatchStat mom : moms) {
            if (moms.size() > 1) {
                mom.setSharedManOfTheMatch(true);
            } else {
                mom.setManOfTheMatch(true);
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<?,?>> incidentEvents = (List<Map<?,?>>) teamMap.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENTS);

        for(Map<?,?> incidentEvent : incidentEvents) {
            Map<?,?> type = (Map<?,?>) incidentEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_TYPE);
            int typeValue = (int) type.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_VALUE);

            if (typeValue == WhoScoredMatchJavaScriptVariables.TYPE_GOAL) {
                WhoScoredGoal goal = new WhoScoredGoal(incidentEvent);
                getGoals().add(goal);
            } else if (typeValue == WhoScoredMatchJavaScriptVariables.TYPE_CARD) {
                // For example Stoke - Everton 6.2 2016
                if (incidentEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_PLAYER_ID) == null) {
                    logger.error("Value playerId missing in incident event {}.", incidentEvent);
                    continue;
                }
                WhoScoredCard card = new WhoScoredCard(incidentEvent);
                getCards().add(card);
            } else if (typeValue == WhoScoredMatchJavaScriptVariables.TYPE_SUBSTITUTION_OFF) {
                WhoScoredSubstitution substitution = new WhoScoredSubstitution(incidentEvent);
                getSubstitutions().add(substitution);
            }
        }

        return team;
    }
    
}
