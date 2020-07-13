package org.d11.camel.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Values in this configuration are decrypted and set by Jasypt from a keys.properties file.
 * @see org.d11.camel.properties.EncryptionConfiguration
 */
@Data
@Configuration
public class KeyProperties {

    @Value("${d11.api.user}")
    private String d11ApiUser;
    @Value("${d11.api.password}")
    private String d11ApiPassword;
    
}
