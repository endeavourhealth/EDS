package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.*;
import org.endeavourhealth.messaging.configuration.schema.pluginContracts.PluginContracts;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.model.ReceivePortProperties;
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

    public IReceivePortHandler getReceivePortHandler(ProtocolType protocol, int port, ReceivePortProperties properties) throws Exception
    {
        for (ReceivePort receivePort : pluginConfiguration.getService().getReceivePorts().getReceivePort())
            if (receivePort.getProtocol().equals(protocol) && receivePort.getPort().equals(port))
                if (properties.matchesConfiguration(receivePort.getProperties()))
                    return (IReceivePortHandler)ReflectionHelper.instantiateObject(receivePort.getReceivePortHandlerClass());

        return null;
    }
}
