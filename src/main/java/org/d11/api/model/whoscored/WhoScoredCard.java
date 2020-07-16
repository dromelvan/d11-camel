package org.d11.api.model.whoscored;

import java.util.Map;

import org.d11.api.model.*;
import org.d11.camel.parser.WhoScoredMatchJavaScriptVariables;

public class WhoScoredCard extends Card {

    public WhoScoredCard(Map<?,?> cardEvent) {
        setPlayer(WhoScoredPlayer.get((int) cardEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_PLAYER_ID)));
        setTeam(WhoScoredTeam.get((int) cardEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_ID)));
        setTime((int) cardEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_MINUTE) + 1);

        Map<?,?> cardEventCardType = (Map<?,?>) cardEvent.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_CARD_TYPE);
        int cardTypeId = (int) cardEventCardType.get(WhoScoredMatchJavaScriptVariables.TEAM_INCIDENT_EVENT_QUALIFIER_VALUE);
        setCardType(cardTypeId == WhoScoredMatchJavaScriptVariables.TYPE_CARD_YELLOW ? CardType.YELLOW.ordinal() : CardType.RED.ordinal());

        PlayerMatchStat playerMatchStat = WhoScoredPlayerMatchStat.get(getPlayer().getWhoScoredId());
        if (getCardType() == CardType.YELLOW.ordinal()) {
            playerMatchStat.setYellowCardTime(getTime());
        } else {
            playerMatchStat.setRedCardTime(getTime());
        }
    }
    
}
