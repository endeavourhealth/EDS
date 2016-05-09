package org.endeavourhealth.core.messaging.model;

import org.endeavourhealth.core.messaging.configuration.Constants;
import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.*;
import org.endeavourhealth.core.messaging.configuration.schema.serviceContracts.ServiceContracts;
import org.endeavourhealth.core.utility.FileHelper;
import org.endeavourhealth.core.utilities.ReflectionHelper;
import org.endeavourhealth.core.utility.XmlHelper;

public class ServicePlugin
{
    private String name;
    private String pathOnDisk;
    private String configurationXmlPath;
    private ServiceConfiguration serviceConfiguration;
    private ServiceContracts serviceContracts;

    public ServicePlugin(String name, String pathOnDisk, String configurationXmlPath) throws Exception
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
        String serviceConfigurationXsd = FileHelper.loadStringResource(ServiceConfiguration.class.getResource(Constants.SERVICE_CONFIGURATION_XSD));

        XmlHelper.validate(configurationXml, serviceConfigurationXsd);

        this.serviceConfiguration = XmlHelper.deserialize(configurationXml, ServiceConfiguration.class);
    }

    public Service.Listeners getListeners()
    {
        return serviceConfiguration.getService().getListeners();
    }

    public String getServiceId()
    {
        return serviceConfiguration.getService().getId();
    }

    public IMessageProcessor getMessageProcessor(String messageTypeId) throws Exception
    {
        for (MessageType messageType : serviceConfiguration.getService().getMessageTypes().getMessageType())
            if (messageType.getId().equals(messageTypeId))
                return (IMessageProcessor)ReflectionHelper.instantiateObject(messageType.getMessageProcessorClass());

        return null;
    }
}
