package org.endeavourhealth.messaging.model;

import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.ReceivePort;

import java.util.HashMap;

public class ReceivePortProperties extends HashMap<String, String>
{
    public static final String Path = "Path";
    public static final String Methods = "Methods";
    public static final String IncludeSubPaths = "IncludeSubPaths";

    private ReceivePortProperties()
    {
    }

    public static ReceivePortProperties fromHttpMessage(HttpMessage httpMessage)
    {
        ReceivePortProperties receivePortProperties = new ReceivePortProperties();
        receivePortProperties.put(ReceivePortProperties.Methods, httpMessage.getHttpMethod());
        receivePortProperties.put(ReceivePortProperties.Path, httpMessage.getPath());

        return receivePortProperties;
    }

    public boolean matchesConfiguration(ReceivePort.Properties configurationProperties)
    {
        return false;
    }
}
