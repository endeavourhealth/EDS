package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.exceptions.ReceiverNotFoundException;
import org.endeavourhealth.messaging.model.IReceiver;

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

    protected Configuration() throws Exception
    {
        loadConfiguration();
    }

    private void loadConfiguration() throws Exception
    {
        this.plugins = PluginLoader.loadPlugins();
    }

    public IReceiver getReceiver(String method, String path) throws Exception
    {
        for (Plugin plugin : plugins)
        {
            IReceiver receiver = plugin.getReceiver(method, path);

            if (receiver != null)
                return receiver;
        }

        throw new ReceiverNotFoundException();
    }

//    public Boolean isContractValid(MessageIdentity messageIdentity) throws MessageNotFoundException
//    {
//        for (Plugin plugin : plugins)
//        {
//            IReceiver endpoint = plugin.isContractValid(messageIdentity);
//
//            if (endpoint != null)
//                return endpoint;
//        }
//
//        return null;
//    }
}
