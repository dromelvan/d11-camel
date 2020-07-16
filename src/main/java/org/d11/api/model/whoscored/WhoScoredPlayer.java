package org.d11.api.model.whoscored;

import java.util.*;

import org.d11.api.model.Player;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;

public class WhoScoredPlayer extends Player {

    private final static Map<Integer, Player> players = new HashMap<Integer, Player>();

    public WhoScoredPlayer(Map<String, Object> player) {
        setWhoScoredId((int) player.get(WhoScoredMatchJavaScriptVariables.PLAYER_ID));
        setName((String) player.get(WhoScoredMatchJavaScriptVariables.PLAYER_NAME));

        players.put(getWhoScoredId(), this);
    }

    protected static Player get(int whoScoredId) {
        return players.get(whoScoredId);
    }
    
}
