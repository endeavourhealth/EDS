package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.exceptions.MessageNotFoundException;
import org.endeavourhealth.messaging.exceptions.ReceiverNotFoundException;
import org.endeavourhealth.messaging.model.IMessageProcessor;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.model.ServicePlugin;
import org.endeavourhealth.messaging.utilities.Log;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class Configuration
{
    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        // TODO - thread safety

        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    private List<ServicePlugin> servicePlugins;

    protected Configuration()
    {
    }

    public String getCodeSourceLocation() throws URISyntaxException
    {
        return new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
    }

    public Boolean isRunningAsJar() throws URISyntaxException
    {
        return getCodeSourceLocation().startsWith("jar:");
    }

    public void loadServicePlugins() throws Exception
    {
        Log.info("Loading service plugins:");

        ServicePluginLoader servicePluginLoader = new ServicePluginLoader(getCodeSourceLocation());

        this.servicePlugins = servicePluginLoader.loadServicePlugins();

        if (servicePlugins.size() > 0)
            servicePlugins.forEach(t -> Log.info(" Loaded plugin [" + t.getName() + "] containing service [" + t.getServiceId() + "]"));
        else
            Log.info(" No service plugins found.");

        Log.info("Service plugins loaded");
    }

    public String getServicePluginPath() throws Exception
    {
        return new ServicePluginLoader(getCodeSourceLocation()).getPluginsPath();
    }

    public List<ServicePlugin> getServicePlugins()
    {
        return servicePlugins;
    }

    public IMessageProcessor getMessageProcessor(MessageIdentity messageIdentity) throws Exception
    {
        for (ServicePlugin servicePlugin : servicePlugins)
        {
            IMessageProcessor messageProcessor = servicePlugin.getMessageProcessor(messageIdentity.getMessageTypeId());

            if (messageProcessor != null)
                return messageProcessor;
        }

        throw new MessageNotFoundException();
    }

    public IReceivePortHandler getReceivePortHandler(String receivePortId) throws Exception
    {
        for (ServicePlugin servicePlugin : servicePlugins)
        {
            IReceivePortHandler receivePortHandler = servicePlugin.getReceivePortHandler(receivePortId);

            if (receivePortHandler != null)
                return receivePortHandler;
        }

        throw new ReceiverNotFoundException();
    }
}
