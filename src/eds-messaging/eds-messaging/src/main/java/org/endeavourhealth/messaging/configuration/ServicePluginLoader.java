package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.Service;
import org.endeavourhealth.messaging.model.ServicePlugin;
import org.endeavourhealth.messaging.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ServicePluginLoader
{
    private String pluginsPath = null;
    private String codeSourceLocation = null;

    public ServicePluginLoader(String codeSourceLocation) throws Exception
    {
        this.codeSourceLocation = codeSourceLocation;

        calculatePluginsPath();
    }

    public String getPluginsPath() throws Exception
    {
        return pluginsPath;
    }

    private void calculatePluginsPath() throws Exception
    {
        String pluginsRootPath = (new File(codeSourceLocation)).getParentFile().getPath();

        pluginsRootPath = FileHelper.combinePaths(pluginsRootPath, Constants.PLUGIN_SUB_DIRECTORY);
        FileHelper.createDirectory(pluginsRootPath);

        pluginsPath = pluginsRootPath;
    }

    public List<ServicePlugin> loadServicePlugins() throws Exception
    {
        List<ServicePlugin> servicePlugins = getServicePlugins();

        addServicePluginsToClasspath(servicePlugins);

        for (ServicePlugin servicePlugin : servicePlugins)
            servicePlugin.initialize();

        return servicePlugins;
    }

    private List<ServicePlugin> getServicePlugins() throws Exception
    {
        List<ServicePlugin> servicePlugins = new ArrayList<>();

        List<File> pluginPaths = Arrays.asList(new File(pluginsPath).listFiles())
                .stream()
                .filter(t -> t.getName().toLowerCase().startsWith(Constants.PLUGIN_PREFIX))
                .collect(Collectors.toList());

        for (File pluginPath : pluginPaths)
        {
            String configurationXmlPath;

            if (pluginPath.isDirectory())
                configurationXmlPath = FileHelper.findFileRecursive(pluginPath, Constants.SERVICE_CONFIGURATION_XML);
            else if (pluginPath.getName().toLowerCase().endsWith(".jar"))
                configurationXmlPath = FileHelper.findFileInJar(pluginPath, Constants.SERVICE_CONFIGURATION_XML);
            else
                continue;

            servicePlugins.add(new ServicePlugin(pluginPath.getName(), pluginPath.getPath(), configurationXmlPath));
        }

        return servicePlugins;
    }

    private void addServicePluginsToClasspath(List<ServicePlugin> servicePlugins) throws Exception
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

        List<URL> urls = new ArrayList<>();

        for (ServicePlugin servicePlugin : servicePlugins)
            urls.add(new File(servicePlugin.getPathOnDisk()).toURI().toURL());

        URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), currentThreadClassLoader);

        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }
}
