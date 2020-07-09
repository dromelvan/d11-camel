package org.d11.camel.properties;

import lombok.Data;

@Data
public class MatchDayEndpoint extends D11RestEndpoint {

    public final static String CURRENT = "current";
    public final static String UPCOMING = "upcoming";
    
    public String matchIdsJsonPath;
    
}
