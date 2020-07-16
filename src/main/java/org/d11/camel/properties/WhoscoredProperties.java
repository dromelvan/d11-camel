package org.d11.camel.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import lombok.Data;

@Data
@Configuration
@PropertySource("classpath:whoscored.properties")
@ConfigurationProperties(prefix = "whoscored")
public class WhoscoredProperties {

    private String matchUrl;
    private String matchTempDirectory;
    private String matchDownloadDirectory;
    private String matchDataDirectory;
    private String parsedMatchDataDirectory;
    
}
