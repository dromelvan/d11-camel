package org.d11.api.model;

import com.fasterxml.jackson.annotation.*;

import lombok.Data;

@Data
@JsonPropertyOrder({ "player", "team", "time", "added_time", "card_type" })
public class Card extends MatchEvent {

    public enum CardType {
        YELLOW,
        RED;
    }

    @JsonProperty("card_type")
    private int cardType;
    
}
