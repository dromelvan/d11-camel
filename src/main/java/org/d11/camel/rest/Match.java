package org.d11.camel.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Match {

    private int id;
    @JsonProperty("whoscored_id")
    private String whoscoredId;
    private String datetime;
    private int status;
    @JsonProperty("match_day_number")
    private int matchDayNumber;
    @JsonProperty("season_name")
    private String seasonName;
    
}
