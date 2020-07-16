package org.d11.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PlayerMatchStat extends D11ApiModel {

    private Player player;
    private Team team;

    private int lineup = 0;
    @JsonProperty("substitution_on_time")
    private int substitutionOnTime = 0;
    @JsonProperty("substitution_off_time")
    private int substitutionOffTime = 0;
    private int goals = 0;
    @JsonProperty("goal_assists")    
    private int goalAssists = 0;
    @JsonProperty("own_goals")
    private int ownGoals = 0;
    @JsonProperty("goals_conceded")
    private int goalsConceded = 0;
    @JsonProperty("yellow_card_time")
    private int yellowCardTime = 0;
    @JsonProperty("red_card_time")
    private int redCardTime = 0;
    @JsonProperty("man_of_the_match")
    private boolean manOfTheMatch = false;
    @JsonProperty("shared_man_of_the_match")
    private boolean sharedManOfTheMatch = false;
    private int rating = 0;
    @JsonProperty("played_position")
    private String playedPosition = "?";
    private int position = 0;
    
}
