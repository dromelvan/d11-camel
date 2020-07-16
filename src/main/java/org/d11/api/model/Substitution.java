package org.d11.api.model;

import com.fasterxml.jackson.annotation.*;

import lombok.Data;

@Data
@JsonPropertyOrder({ "player", "player_in", "team", "time", "added_time" })
public class Substitution extends MatchEvent {

    @JsonProperty("player_in")
    private Player playerIn;

}
