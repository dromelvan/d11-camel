package org.d11.api.model.whoscored;

import java.util.Map;

import org.d11.api.model.*;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;

public class WhoScoredSubstitution extends Substitution {

    public WhoScoredSubstitution(Map<?,?> substitutionEvent) {
        setPlayer(WhoScoredPlayer.get((int) substitutionEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_PLAYER_ID)));
        setTime((int) substitutionEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_MINUTE) + 1);
        setPlayerIn(WhoScoredPlayer.get((int) substitutionEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_RELATED_PLAYER_ID)));
        setTeam(WhoScoredTeam.get((int) substitutionEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_ID)));

        PlayerMatchStat playerMatchStat = WhoScoredPlayerMatchStat.get(getPlayer().getWhoScoredId());
        playerMatchStat.setSubstitutionOffTime(getTime());

        playerMatchStat = WhoScoredPlayerMatchStat.get(getPlayerIn().getWhoScoredId());
        playerMatchStat.setSubstitutionOnTime(getTime());
    }
    
}
