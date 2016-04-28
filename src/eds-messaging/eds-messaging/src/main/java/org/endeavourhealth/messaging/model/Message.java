package org.endeavourhealth.messaging.model;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Message
{
    protected String body;
    protected HashMap<String, String> headers = new HashMap<>();
    protected MessageIdentity messageIdentity;

    public String getBody()
    {
        return body;
    }

    public ArrayList<String> getHeaderNames()
    {
        return new ArrayList<>(headers.keySet());
    }

    public String getHeaderValue(String headerName)
    {
        return headers.get(headerName);
    }

    public MessageIdentity getMessageIdentity()
    {
        return messageIdentity;
    }

    public void setMessageIdentity(MessageIdentity messageIdentity)
    {
        this.messageIdentity = messageIdentity;
    }
}
