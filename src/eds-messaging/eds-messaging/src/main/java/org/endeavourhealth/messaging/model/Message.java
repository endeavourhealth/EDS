package org.endeavourhealth.messaging.model;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Message
{
    private String httpMethod;
    private String path;
    private String queryString;
    private String body;
    private HashMap<String, String> headers = new HashMap<>();

    public static Message fromServletRequest(HttpServletRequest request) throws IOException
    {
        Message message = new Message();
        message.httpMethod = request.getMethod();
        message.path = request.getPathInfo();
        message.queryString = request.getQueryString();
        message.body = IOUtils.toString(request.getReader());

        for (String headerName : Collections.list(request.getHeaderNames()))
            message.headers.put(headerName, request.getHeader(headerName));

        return message;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public String getBody()
    {
        return body;
    }

    public String getPath()
    {
        return path;
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
