package org.d11.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MatchEvent extends D11ApiModel {

    private Team team;
    private Player player;
    private int time;
    @JsonProperty("added_time")
    private int addedTime;
    
    public void setTime(Integer time) {
        if(time > 90) {
            setAddedTime(time - 90);
            time = 90;
        }
        this.time = time;
    }
    
}
