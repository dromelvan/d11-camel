package org.d11.api.model.whoscored;

import java.math.BigDecimal;
import java.util.*;

import org.d11.api.model.*;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;
import org.slf4j.*;

public class WhoScoredPlayerMatchStat extends PlayerMatchStat {

    private final static Logger logger = LoggerFactory.getLogger(WhoScoredPlayerMatchStat.class);
    private final static Map<Integer, WhoScoredPlayerMatchStat> playerMatchStats = new HashMap<Integer, WhoScoredPlayerMatchStat>();

    public WhoScoredPlayerMatchStat(Map<String, Object> playerMatchStat) {
        Player player = new WhoScoredPlayer(playerMatchStat);
        setPlayer(player);

        setLineup((((String) playerMatchStat.get(WhoScoredMatchJavaScriptVariables.PLAYER_POSITION)).toUpperCase().equals(WhoScoredMatchJavaScriptVariables.PLAYER_POSITION_SUBSTITUTE) ? 1 : 2));
        setPlayedPosition((String) playerMatchStat.get(WhoScoredMatchJavaScriptVariables.PLAYER_POSITION));
        setPosition(getPosition(getPlayedPosition()));

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) playerMatchStat.get(WhoScoredMatchJavaScriptVariables.PLAYER_STATS);
        if (stats != null) {
            @SuppressWarnings("unchecked")
            List<Object> ratings = (List<Object>) stats.get(WhoScoredMatchJavaScriptVariables.PLAYER_STATS_RATINGS);
            if (ratings != null) {
                Object ratingObject = ratings.get(ratings.size() - 1);
                if (ratingObject instanceof Double) {
                    BigDecimal bigDecimal = BigDecimal.valueOf((Double) ratingObject);
                    bigDecimal = bigDecimal.movePointRight(2);
                    setRating(bigDecimal.intValue());
                } else if (ratingObject instanceof Integer) {
                    setRating((int) ratingObject * 100);
                }
            }
        }
        playerMatchStats.put(getPlayer().getWhoScoredId(), this);
    }

    private int getPosition(String playedPosition) {
        switch (playedPosition.toUpperCase()) {
            case "GK":
                return 1;
            case "DC":
            case "DL":
            case "DR":
                return 3;
            case "MC":
            case "ML":
            case "MR":
            case "DMC":
            case "DML":
            case "DMR":
            case "AMC":
            case "AML":
            case "AMR":
                return 4;
            case "FW":
            case "FWL":
            case "FWR":
                return 5;
            case "SUB":
                return 6;
            default:
                logger.warn("Unknown played position {}.", getPlayedPosition());
                return 6;
        }
    }

    protected static PlayerMatchStat get(int whoScoredId) {
        return playerMatchStats.get(whoScoredId);
    }
    
}
