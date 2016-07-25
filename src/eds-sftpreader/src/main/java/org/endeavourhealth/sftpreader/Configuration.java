package org.endeavourhealth.sftpreader;

import com.google.common.io.Resources;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Configuration
{
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String CONFIG_XSD = "SftpReaderConfiguration.xsd";
    private static final String CONFIG_RESOURCE = "SftpReaderConfiguration.xml";

    public static void initialiseEngineConfiguration(String[] args) throws Exception
    {
        EngineConfigurationSerializer.loadConfigFromArgIfPossible(args, 1);
    }

    public static SftpReaderConfiguration loadLocalConfiguration(String[] args) throws Exception
    {
        SftpReaderConfiguration configuration = null;

        if (args.length > 0)
        {
            LOG.info("Loading local configuration file (" + args[0] + ")");
            configuration = XmlSerializer.deserializeFromFile(SftpReaderConfiguration.class, args[0], CONFIG_XSD);
        }
        else
        {
            LOG.info("Loading local configuration file from resource " + CONFIG_RESOURCE);
            configuration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
        }

        resolveFilePaths(configuration);

        return configuration;
    }

    private static void resolveFilePaths(SftpReaderConfiguration configuration)
    {
        configuration.getSftpCredentials().setClientPrivateKeyFilePath(resolveFilePath(configuration.getSftpCredentials().getClientPrivateKeyFilePath()));
        configuration.getSftpCredentials().setHostPublicKeyFilePath(resolveFilePath(configuration.getSftpCredentials().getHostPublicKeyFilePath()));

        if (configuration.getPgpDecryption() != null)
        {
            configuration.getPgpDecryption().setRecipientPrivateKeyFilePath(resolveFilePath(configuration.getPgpDecryption().getRecipientPrivateKeyFilePath()));
            configuration.getPgpDecryption().setSenderPublicKeyFilePath(resolveFilePath(configuration.getPgpDecryption().getSenderPublicKeyFilePath()));
        }
    }

    private static String resolveFilePath(String filePath)
    {
        if (!Files.exists(Paths.get(filePath)))
            return Resources.getResource(filePath).getPath();

        return filePath;
    }
}
