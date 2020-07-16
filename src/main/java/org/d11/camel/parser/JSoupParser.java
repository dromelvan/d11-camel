package org.d11.camel.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class JSoupParser<T extends Object> extends D11Parser<T> {

    @Override
    public T doParse(String source) {
        try {
            Document document = Jsoup.parse(source, "UTF-8");
            return parseDocument(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract T parseDocument(Document document);
}
