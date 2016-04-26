package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.exceptions.ReceiverNotFoundException;
import org.endeavourhealth.messaging.model.IReceivePortHandler;
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

    public String getCodeSourceLocation() throws URISyntaxException
    {
        return new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
    }

    public Boolean isRunningAsJar() throws URISyntaxException
    {
        return getCodeSourceLocation().startsWith("jar:");
    }

    public String getPluginPath() throws Exception
    {
        return new PluginLoader(getCodeSourceLocation()).getPluginsPath();
    }

    public List<Plugin> getPlugins()
    {
        return plugins;
    }

    public IReceivePortHandler getReceivePortHandler(String method, String path) throws Exception
    {
//        for (Plugin plugin : plugins)
//        {
//            IReceivePortHandler receiver = plugin.getReceiver(method, path);
//
//            if (receiver != null)
//                return receiver;
//        }

        throw new ReceiverNotFoundException();
    }

//    public Boolean isContractValid(MessageIdentity messageIdentity) throws MessageNotFoundException
//    {
//        for (Plugin plugin : plugins)
//        {
//            IReceivePortHandler endpoint = plugin.isContractValid(messageIdentity);
//
//            if (endpoint != null)
//                return endpoint;
//        }
//
//        return null;
//    }
}
