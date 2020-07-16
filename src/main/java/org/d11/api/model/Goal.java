package org.d11.api.model;

import com.fasterxml.jackson.annotation.*;

import lombok.Data;

@Data
@JsonPropertyOrder({ "player", "team", "time", "added_time", "penalty", "own_goal" })
public class Goal extends MatchEvent {

    private Boolean penalty = false;
    @JsonProperty("own_goal")
    private Boolean ownGoal;
    
}
