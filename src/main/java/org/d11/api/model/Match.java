package org.d11.api.model;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "whoscored_id", "datetime", "elapsed", "status", "match_day_number", "season_name", "home_team", "away_team", "player_match_stats", "goals", "cards", "substitutions" })
public class Match extends D11ApiModel {

    private int id;
    @JsonProperty("whoscored_id")
    private int whoScoredId;
    private String datetime;
    private String elapsed;
    private int status;
    @JsonProperty("match_day_number")
    private int matchDayNumber;
    @JsonProperty("season_name")
    private String seasonName;

    @JsonProperty("home_team")
    private Team homeTeam;
    @JsonProperty("away_team")
    private Team awayTeam;
    
    @JsonProperty("player_match_stats")
    private List<PlayerMatchStat> playerMatchStats = new ArrayList<PlayerMatchStat>();
    private List<Goal> goals = new ArrayList<Goal>();
    private List<Card> cards = new ArrayList<Card>();
    private List<Substitution> substitutions = new ArrayList<Substitution>();
    
}
