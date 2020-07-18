package org.d11.camel.properties;

import lombok.Data;

@Data
public class MatchEndpoint extends D11RestEndpoint {

    public String idJsonPath;
    public String datetimeJsonPath;
    
}
