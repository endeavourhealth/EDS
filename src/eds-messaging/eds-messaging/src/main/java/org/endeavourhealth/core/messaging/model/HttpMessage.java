package org.endeavourhealth.core.messaging.model;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

public class HttpMessage extends Message
{
    protected String httpMethod;
    protected String path;
    protected String queryString;

    public static HttpMessage fromServletRequest(HttpServletRequest request) throws IOException
    {
        HttpMessage message = new HttpMessage();
        message.httpMethod = request.getMethod();
        message.path = request.getPathInfo();
        message.queryString = request.getQueryString();
        message.body = IOUtils.toString(request.getReader());

        for (Object headerName : Collections.list(request.getHeaderNames()))
            message.headers.put((String)headerName, request.getHeader((String)headerName));

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

    public String getPath()
    {
        return path;
    }
}
