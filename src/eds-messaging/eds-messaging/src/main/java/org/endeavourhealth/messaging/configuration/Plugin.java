package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.*;
import org.endeavourhealth.messaging.configuration.schema.pluginContracts.PluginContracts;
import org.endeavourhealth.messaging.model.IMessageProcessor;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.utilities.FileHelper;
import org.endeavourhealth.messaging.utilities.ReflectionHelper;
import org.endeavourhealth.messaging.utilities.XmlHelper;

import java.util.List;

public class Plugin
{
    private String name;
    private String pathOnDisk;
    private String configurationXmlPath;
    private PluginConfiguration pluginConfiguration;
    private PluginContracts pluginContracts;

    public Plugin(String name, String pathOnDisk, String configurationXmlPath) throws Exception
    {
        this.name = name;
        this.pathOnDisk = pathOnDisk;
        this.configurationXmlPath = configurationXmlPath;
    }

    public String getName()
    {
        return name;
    }

    public String getPathOnDisk()
    {
        return pathOnDisk;
    }

    public void initialize() throws Exception
    {
        String configurationXml = FileHelper.loadStringFile(configurationXmlPath);
        String routeConfigurationXsd = FileHelper.loadStringResource(PluginConfiguration.class.getResource(Constants.PLUGIN_CONFIGURATION_XSD));

        XmlHelper.validate(configurationXml, routeConfigurationXsd);

        this.pluginConfiguration = XmlHelper.deserialize(configurationXml, PluginConfiguration.class);
    }

    public List<ReceivePort> getReceivePorts()
    {
        return pluginConfiguration.getService().getReceivePorts().getReceivePort();
    }

    public IReceivePortHandler getReceivePortHandler(String receivePortId) throws Exception
    {
        for (ReceivePort receivePort : pluginConfiguration.getService().getReceivePorts().getReceivePort())
            if (receivePort.getId().equals(receivePortId))
                return (IReceivePortHandler)ReflectionHelper.instantiateObject(receivePort.getReceivePortHandlerClass());

        return null;
    }

    public IMessageProcessor getMessageProcessor(String messageTypeId) throws Exception
    {
        for (MessageType messageType : pluginConfiguration.getService().getMessageTypes().getMessageType())
            if (messageType.getId().equals(messageTypeId))
                return (IMessageProcessor)ReflectionHelper.instantiateObject(messageType.getMessageProcessorClass());

        return null;
    }
}
