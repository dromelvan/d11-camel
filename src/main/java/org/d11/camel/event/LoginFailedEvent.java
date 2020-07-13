package org.d11.camel.event;

import org.springframework.context.ApplicationEvent;

public class LoginFailedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -3655417031207191459L;

    public LoginFailedEvent(Object source) {
        super(source);
    }

}
