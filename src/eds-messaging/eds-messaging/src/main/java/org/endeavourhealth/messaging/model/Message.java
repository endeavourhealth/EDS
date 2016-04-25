package org.endeavourhealth.messaging.model;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Message
{
    protected String body;
    protected HashMap<String, String> headers = new HashMap<>();

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
}
