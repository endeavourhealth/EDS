package org.endeavourhealth.messaging.configuration;

import org.apache.commons.io.IOUtils;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.*;
import org.endeavourhealth.messaging.configuration.schema.pluginContracts.PluginContracts;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.utilities.FileHelper;
import org.endeavourhealth.messaging.utilities.XmlHelper;

import java.util.Arrays;
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

    public String getConfigurationXmlPath()
    {
        return configurationXmlPath;
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

    public IReceivePortHandler getReceivePortHandler(String protocol, String method, String path) throws Exception
    {
//        for (ServiceType serviceType : pluginConfiguration.getServices().getService())
//        {
//            for (ReceivePortType receivePort : serviceType.getReceivePorts().getReceivePort())
//            {
//                if ((receivePort.isIncludeSubPaths() && (path.startsWith(receivePort.getPath())))
//                        || (receiver.getPath().equals(path)))
//                {
//                    if (Arrays.asList(receiver.getMethods().split(",")).contains(method.toLowerCase()))
//                        return (IReceivePortHandler) Thread.currentThread().getContextClassLoader().loadClass(receiver.getReceiverClass()).newInstance();
//
//                    throw new ReceiverMethodNotSupportedException();
//                }
//            }
//        }

        return null;
    }
//
//    public Boolean isContractValid(MessageIdentity messageIdentity) throws MessageNotFoundException
//    {
//        if (messageIdentity == null)
//            return false;
//
//        String messageId = getMessageId(messageIdentity.getMessageName(), messageIdentity.getVersion());
//
//        if (StringUtils.isBlank(messageId))
//            throw new MessageNotFoundException("Message not found with name = " + messageIdentity.getMessageName() + " and version = " + messageIdentity.getVersion());
//
//        for (Contract contract : pluginContracts.getContracts().getContract())
//        {
//            if ((StringUtils.equals(contract.getMessageId(), messageId))
//                    && (StringUtils.equals(contract.getSender(), messageIdentity.getSender()))
//                    && (StringUtils.equals(contract.getReceiver(), messageIdentity.getRecipient())))
//            {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    public String getMessageId(String messageName, String version)
//    {
//        for (Message message : pluginConfiguration.getMessages().getMessage())
//            if (StringUtils.equals(message.getName(), messageName))
//                if (StringUtils.equals(version, message.getVersion()))
//                    return message.getMessageId();
//
//        return null;
//    }
}
