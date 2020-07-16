package org.d11.camel.parser;

import org.slf4j.*;

public abstract class D11Parser<T extends Object> {

    private final static Logger logger = LoggerFactory.getLogger(D11Parser.class);
    
    public T parse(String source) {
        try {
            return doParse(source);
        } catch (Exception e) {
            logger.error("Error while parsing. Error details:", e);
        }
        return null;        
    }
    
    protected abstract T doParse(String source);
    
}
