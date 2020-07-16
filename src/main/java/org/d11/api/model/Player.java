package org.d11.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Player extends D11ApiModel {

    @JsonProperty("whoscored_id")    
    private int whoScoredId;
    private String name;
    
}
