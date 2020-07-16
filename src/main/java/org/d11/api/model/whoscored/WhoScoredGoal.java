package org.d11.api.model.whoscored;

import java.util.*;

import org.d11.api.model.*;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;

public class WhoScoredGoal extends Goal {

    public WhoScoredGoal(Map<?,?> goalEvent) {
        setPlayer(WhoScoredPlayer.get((int) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_PLAYER_ID)));
        setTeam(WhoScoredTeam.get((int) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_ID)));
        setTime((int) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_MINUTE) + 1);
        Boolean ownGoal = (Boolean) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_OWN_GOAL);
        setOwnGoal(ownGoal == null ? false : ownGoal);

        @SuppressWarnings("unchecked")
        List<Map<?,?>> qualifiers = (List<Map<?,?>>) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_QUALIFIERS);
        for (Map<?,?> qualifier : qualifiers) {
            Map<?,?> typeMap = (Map<?,?>) qualifier.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_QUALIFIER_TYPE);
            int value = (int) typeMap.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_QUALIFIER_VALUE);
            if (value == WhoScoredMatchJavaScriptVariables.TYPE_PENALTY) {
                setPenalty(true);
                break;
            }
        }

        PlayerMatchStat playerMatchStat = WhoScoredPlayerMatchStat.get(getPlayer().getWhoScoredId());
        if (!getOwnGoal()) {
            playerMatchStat.setGoals(playerMatchStat.getGoals() + 1);
        } else {
            playerMatchStat.setOwnGoals(playerMatchStat.getGoals() + 1);
        }

        if (goalEvent.containsKey(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_RELATED_PLAYER_ID)) {
            playerMatchStat = WhoScoredPlayerMatchStat.get((int) goalEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_RELATED_PLAYER_ID));
            if (playerMatchStat != null) {
                playerMatchStat.setGoalAssists(playerMatchStat.getGoalAssists() + 1);
            }
        }
    }
    
}
