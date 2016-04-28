package org.endeavourhealth.messaging.model;

import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.ReceivePort;

import java.util.HashMap;

public class ReceivePortProperties extends HashMap<String, String>
{
    public static final String Path = "Path";
    public static final String Methods = "Methods";
    public static final String IncludeSubPaths = "IncludeSubPaths";

    private ReceivePortProperties()
    {
    }

    public static ReceivePortProperties fromConfiguration(ReceivePort.Properties configurationProperties)
    {
        ReceivePortProperties receivePortProperties = new ReceivePortProperties();

        for (ReceivePort.Properties.Entry entry : configurationProperties.getEntry())
            receivePortProperties.put(entry.getKey(), entry.getValue());

        return receivePortProperties;
    }

    public static ReceivePortProperties fromHttpMessage(HttpMessage httpMessage)
    {
        ReceivePortProperties receivePortProperties = new ReceivePortProperties();
        receivePortProperties.put(ReceivePortProperties.Methods, httpMessage.getHttpMethod());
        receivePortProperties.put(ReceivePortProperties.Path, httpMessage.getPath());

        return receivePortProperties;
    }

    public String getPath()
    {
        return get(Path);
    }

    public String getMethods()
    {
        return get(Methods);
    }

    public String getIncludeSubPaths()
    {
        return get(IncludeSubPaths);
    }
}
