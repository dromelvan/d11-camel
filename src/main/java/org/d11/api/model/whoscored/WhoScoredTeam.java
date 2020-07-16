package org.d11.api.model.whoscored;

import java.util.*;

import org.d11.api.model.Team;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WhoScoredTeam extends Team {

    private final static Map<Integer, Team> teams = new HashMap<Integer, Team>();

    public WhoScoredTeam(int whoScoredId, String name) {
        setWhoScoredId(whoScoredId);
        setName(name);
    }
    
    public WhoScoredTeam(Map<String, Object> team) {
        setName((String) team.get(WhoScoredMatchJavaScriptVariables.TEAM_NAME));
        setWhoScoredId((int) team.get(WhoScoredMatchJavaScriptVariables.TEAM_ID));

        teams.put(getWhoScoredId(), this);
    }

    protected static Team get(int whoScoredId) {
        return teams.get(whoScoredId);
    }
    
}
