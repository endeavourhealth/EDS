package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class PluginLoader
{

    private String pluginsPath = null;
    private String codeSourceLocation = null;

    public PluginLoader(String codeSourceLocation) throws Exception
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

    public List<Plugin> loadPlugins() throws Exception
    {
        List<Plugin> plugins = getPlugins();

        addPluginsToClasspath(plugins);

        for (Plugin plugin : plugins)
            plugin.initialize();

        return plugins;
    }

    private List<Plugin> getPlugins() throws Exception
    {
        List<Plugin> plugins = new ArrayList<>();

        List<File> pluginPaths = Arrays.asList(new File(pluginsPath).listFiles())
                .stream()
                .filter(t -> t.getName().toLowerCase().startsWith(Constants.PLUGIN_PREFIX))
                .collect(Collectors.toList());

        for (File pluginPath : pluginPaths)
        {
            if (pluginPath.isDirectory())
            {
                String absoluteConfigurationXmlPath = FileHelper.findFileRecursive(pluginPath, Constants.PLUGIN_CONFIGURATION_XML);
                //String configurationXmlPath = pluginDirectory.toURI().relativize(new File(absoluteConfigurationXmlPath).toURI()).getPath();

                plugins.add(new Plugin(pluginPath.getName(), pluginPath.getPath(), absoluteConfigurationXmlPath));
            }
            else if (pluginPath.getName().toLowerCase().endsWith(".jar"))
            {
                String configurationXmlPath = FileHelper.findFileInJar(pluginPath, Constants.PLUGIN_CONFIGURATION_XML);
                plugins.add(new Plugin(pluginPath.getName(), pluginPath.getPath(), configurationXmlPath));
            }
        }

        return plugins;
    }

    private void addPluginsToClasspath(List<Plugin> plugins) throws Exception
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

        List<URL> urls = new ArrayList<>();

        for (Plugin plugin : plugins)
            urls.add(new File(plugin.getPathOnDisk()).toURI().toURL());

        URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), currentThreadClassLoader);

        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }
}
