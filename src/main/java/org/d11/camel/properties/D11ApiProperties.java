package org.d11.camel.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import lombok.Data;

@Data
@Configuration
@PropertySource("classpath:d11-api.properties")
@ConfigurationProperties(prefix = "d11.api")
public class D11ApiProperties {

    private String host;
    private String port;
    private String version;
    
    private String url;
    
    private String postponedDatetime;
    
    private D11RestEndpoint login;
    private SeasonEndpoint currentSeason;
    private MatchDayEndpoint matchDay;
    private MatchEndpoint match;
    private D11RestEndpoint matchDateTime;
 
    public String getBaseUrl() {
        return String.format(getUrl(), getHost(), getPort(), getVersion(), "");
    }
        
}
