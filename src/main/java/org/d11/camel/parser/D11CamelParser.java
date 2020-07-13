package org.d11.camel.parser;

public abstract class D11CamelParser<T extends Object> {

    public abstract T parse(String html);
    
}
