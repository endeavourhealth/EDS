package org.endeavourhealth.messaging.configuration;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.Receiver;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.Message;
import org.endeavourhealth.messaging.configuration.schema.pluginConfiguration.PluginConfiguration;
import org.endeavourhealth.messaging.configuration.schema.pluginContracts.Contract;
import org.endeavourhealth.messaging.configuration.schema.pluginContracts.PluginContracts;
import org.endeavourhealth.messaging.exceptions.MessageNotFoundException;
import org.endeavourhealth.messaging.exceptions.ReceiverMethodNotSupportedException;
import org.endeavourhealth.messaging.model.IReceiver;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.utilities.FileHelper;
import org.endeavourhealth.messaging.utilities.XmlHelper;

import java.util.Arrays;

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
        String configurationXml = FileHelper.loadStringResource(configurationXmlPath);
        this.pluginConfiguration = XmlHelper.deserialize(configurationXml, PluginConfiguration.class);
    }

    public int getPort()
    {
        return pluginConfiguration.getListener().getPort();
    }

    public IReceiver getReceiver(String method, String path) throws Exception
    {
        for (Receiver receiver : pluginConfiguration.getListener().getReceivers().getReceiver())
        {
            if ((receiver.isIncludeSubPaths() && (path.startsWith(receiver.getPath())))
                    || (receiver.getPath().equals(path)))
            {
                if (Arrays.asList(receiver.getMethods().split(",")).contains(method.toLowerCase()))
                    return (IReceiver)Thread.currentThread().getContextClassLoader().loadClass(receiver.getReceiverClass()).newInstance();

                throw new ReceiverMethodNotSupportedException();
            }
        }

        return null;
    }

    public Boolean isContractValid(MessageIdentity messageIdentity) throws MessageNotFoundException
    {
        if (messageIdentity == null)
            return false;

        String messageId = getMessageId(messageIdentity.getMessageName(), messageIdentity.getVersion());

        if (StringUtils.isBlank(messageId))
            throw new MessageNotFoundException("Message not found with name = " + messageIdentity.getMessageName() + " and version = " + messageIdentity.getVersion());

        for (Contract contract : pluginContracts.getContracts().getContract())
        {
            if ((StringUtils.equals(contract.getMessageId(), messageId))
                    && (StringUtils.equals(contract.getSender(), messageIdentity.getSender()))
                    && (StringUtils.equals(contract.getReceiver(), messageIdentity.getRecipient())))
            {
                return true;
            }
        }

        return false;
    }

    public String getMessageId(String messageName, String version)
    {
        for (Message message : pluginConfiguration.getMessages().getMessage())
            if (StringUtils.equals(message.getName(), messageName))
                if (StringUtils.equals(version, message.getVersion()))
                    return message.getMessageId();

        return null;
    }
}
