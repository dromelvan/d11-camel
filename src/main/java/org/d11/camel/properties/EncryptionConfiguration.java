package org.d11.camel.properties;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.*;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;

@Configuration
@EncryptablePropertySource("classpath:keys.properties")
public class EncryptionConfiguration {

    private SimpleStringPBEConfig simpleStringPBEConfig;
    // This is the password used when none is available in the application argument
    // or system properties, like during testing and development.
    public final static String DEFAULT_ENCRYPTION_PASSWORD = "password";
    // This is the option in the application arguments or system properties where we
    // expect the password to be provided.
    public final static String ENCRYPTION_PASSWORD_APPLICATION_ARGUMENT_OPTION = "encryption.configuration.password";

    @Autowired
    public EncryptionConfiguration(ApplicationArguments applicationArguments) {
        this.simpleStringPBEConfig = new SimpleStringPBEConfig();
        if (applicationArguments.containsOption(ENCRYPTION_PASSWORD_APPLICATION_ARGUMENT_OPTION)) {
            simpleStringPBEConfig.setPassword(applicationArguments.getOptionValues(ENCRYPTION_PASSWORD_APPLICATION_ARGUMENT_OPTION).get(0));
        } else if (System.getProperty(ENCRYPTION_PASSWORD_APPLICATION_ARGUMENT_OPTION) != null) {
            simpleStringPBEConfig.setPassword(System.getProperty(ENCRYPTION_PASSWORD_APPLICATION_ARGUMENT_OPTION));
        } else {
            simpleStringPBEConfig.setPassword(DEFAULT_ENCRYPTION_PASSWORD);
        }
        simpleStringPBEConfig.setPoolSize("1");
    }

    @Bean(name = "encryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(this.simpleStringPBEConfig);
        return encryptor;
    }

}
