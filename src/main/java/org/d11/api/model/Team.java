package org.d11.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team extends D11ApiModel {

    private String name;
    @JsonProperty("whoscored_id")
    private int whoScoredId;

}
