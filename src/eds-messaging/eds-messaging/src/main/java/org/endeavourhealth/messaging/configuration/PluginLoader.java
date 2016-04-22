package org.endeavourhealth.messaging.configuration;

import org.endeavourhealth.messaging.utilities.FileHelper;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class PluginLoader
{
    private static final String PLUGIN_PREFIX = "org.endeavour.resolution.plugin.";
    private static final String PLUGIN_JAR_DIRECTORY = "plugins";
    private static final String PLUGIN_CONFIGURATION_XML = "PluginConfiguration.xml";

    private PluginLoader()
    {
    }

    public static List<Plugin> loadPlugins() throws Exception
    {
        File rootPath = new File(PluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

        boolean runningAsJar = (!rootPath.isDirectory());

        String pluginsRootPath = rootPath.getParentFile().getPath();

        if (runningAsJar)
        {
            pluginsRootPath = FileHelper.combinePaths(pluginsRootPath, PLUGIN_JAR_DIRECTORY);
            FileHelper.createDirectory(pluginsRootPath);
        }

        List<Plugin> plugins = (runningAsJar ? getJarPlugins(pluginsRootPath) : getDirectoryPlugins(pluginsRootPath));

        addPluginsToClasspath(plugins);

        for (Plugin plugin : plugins)
            plugin.initialize();

        return plugins;
    }

    private static List<Plugin> getJarPlugins(String pluginRootPath) throws Exception
    {
        List<Plugin> plugins = new ArrayList<>();

        List<File> jarPaths = Arrays.asList(new File(pluginRootPath).listFiles())
                .stream()
                .filter(t -> (!t.isDirectory()) && (t.getName().toLowerCase().startsWith(PLUGIN_PREFIX)) && (t.getName().toLowerCase().endsWith(".jar")))
                .collect(Collectors.toList());

        for (File jarPath : jarPaths)
        {
            String configurationXmlPath = FileHelper.findFileInJar(jarPath, PLUGIN_CONFIGURATION_XML);
            plugins.add(new Plugin(jarPath.getName(), jarPath.getPath(), configurationXmlPath));
        }

        return plugins;
    }

    private static List<Plugin> getDirectoryPlugins(String pluginRootPath) throws Exception
    {
        List<Plugin> plugins = new ArrayList<>();

        List<File> pluginDirectories = Arrays.asList(new File(pluginRootPath).listFiles())
                .stream()
                .filter(t -> t.isDirectory() && t.getName().toLowerCase().startsWith(PLUGIN_PREFIX))
                .collect(Collectors.toList());

        for (File pluginDirectory : pluginDirectories)
        {
            String absoluteConfigurationXmlPath = FileHelper.findFileRecursive(pluginDirectory, PLUGIN_CONFIGURATION_XML);
            String configurationXmlPath = pluginDirectory.toURI().relativize(new File(absoluteConfigurationXmlPath).toURI()).getPath();

            plugins.add(new Plugin(pluginDirectory.getName(), pluginDirectory.getPath(), configurationXmlPath));
        }

        return plugins;
    }

    private static void addPluginsToClasspath(List<Plugin> plugins) throws Exception
    {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

        List<URL> urls = new ArrayList<>();

        for (Plugin plugin : plugins)
            urls.add(new File(plugin.getPathOnDisk()).toURI().toURL());

        URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), currentThreadClassLoader);

        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }
}
