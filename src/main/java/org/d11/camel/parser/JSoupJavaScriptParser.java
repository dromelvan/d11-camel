package org.d11.camel.parser;

import java.util.*;

import javax.script.*;

import org.jsoup.nodes.*;
import org.slf4j.*;

public abstract class JSoupJavaScriptParser<T extends Object, U extends JavaScriptVariables> extends JSoupParser<T> {

    private U javaScriptVariables;
    private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final static Logger logger = LoggerFactory.getLogger(JSoupJavaScriptParser.class);

    public JSoupJavaScriptParser(U javaScriptVariables) {
        this.javaScriptVariables = javaScriptVariables;
    }
    
    public U getJavaScriptVariables(Document document) {
        for (Element element : document.getElementsByTag("script")) {
            try {
                this.scriptEngine.eval(element.data());
            } catch (ScriptException e) {
                logger.trace("Could not parse javascript: {}\n{}.", element.data(), e.getMessage());
            }
        }

        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);

        for (String key : bindings.keySet()) {
            Object value = bindings.get(key);
            this.javaScriptVariables.put(key, parseJavaScriptValue(value));
        }

        this.javaScriptVariables.init();
        return this.javaScriptVariables;
    }
    
    private Object parseJavaScriptValue(Object value) {
        if(value instanceof Map) {
            return parseJavaScriptMap((Map<?,?>) value);
        }
        return value;
    }

    private Object parseJavaScriptMap(Map<?,?> map) {
        int maxIndex = -1;
        for(Object key : map.keySet()) {
            try {
                int index = Integer.parseInt((String) key);
                if(index > maxIndex) {
                    maxIndex = index;
                }
            } catch(Exception e) {
                maxIndex = -1;
                break;
            }
        }

        if(maxIndex >= 0) {
            List<Object> values = new ArrayList<>();
            for(int i = 0; i <= maxIndex; ++i) {
                Object value = parseJavaScriptValue(map.get(String.valueOf(i)));
                values.add(value);
            }
            return values;
        } else {
            Map<Object, Object> values = new HashMap<>();
            for(Object key : map.keySet()) {
                Object value = map.get(key);
                values.put(key, parseJavaScriptValue(value));
            }
            return values;
        }
    }
    
}
