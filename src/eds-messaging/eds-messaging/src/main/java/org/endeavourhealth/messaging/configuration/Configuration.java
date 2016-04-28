package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.exceptions.MessageNotFoundException;
import org.endeavourhealth.messaging.exceptions.ReceiverNotFoundException;
import org.endeavourhealth.messaging.model.IMessageProcessor;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
import org.endeavourhealth.messaging.model.MessageIdentity;
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

    private List<Plugin> plugins;

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


    public void loadPlugins() throws Exception
    {
        Log.info("Loading plugins:");

        PluginLoader pluginLoader = new PluginLoader(getCodeSourceLocation());

        this.plugins = pluginLoader.loadPlugins();

        if (plugins.size() > 0)
            plugins.forEach(t -> Log.info(" Loaded plugin '" + t.getName() + "'."));
        else
            Log.info(" No plugins found.");
    }

    public String getPluginPath() throws Exception
    {
        return new PluginLoader(getCodeSourceLocation()).getPluginsPath();
    }

    public List<Plugin> getPlugins()
    {
        return plugins;
    }

    public IMessageProcessor getMessageProcessor(MessageIdentity messageIdentity) throws Exception
    {
        for (Plugin plugin : plugins)
        {
            IMessageProcessor messageProcessor = plugin.getMessageProcessor(messageIdentity.getMessageTypeId());

            if (messageProcessor != null)
                return messageProcessor;
        }

        throw new MessageNotFoundException();
    }

    public IReceivePortHandler getReceivePortHandler(String receivePortId) throws Exception
    {
        for (Plugin plugin : plugins)
        {
            IReceivePortHandler receivePortHandler = plugin.getReceivePortHandler(receivePortId);

            if (receivePortHandler != null)
                return receivePortHandler;
        }

        throw new ReceiverNotFoundException();
    }
}
