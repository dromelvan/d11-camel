package org.d11.camel.properties;

import lombok.Data;

@Data
public class MatchDayEndpoint extends D11RestEndpoint {

    public String matchIdsJsonPath;
    
}
