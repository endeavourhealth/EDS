using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml.Linq;

namespace Endeavour.EmisWebPollerService
{
    internal static class ConfigurationManager
    {
        public static Configuration.Configuration ReadConfiguration()
        {
            try
            {
                Log.Write("Reading configuration from '{0}'.", FilePaths.ConfigurationFilePath);

                if (!File.Exists(FilePaths.ConfigurationFilePath))
                    SaveNewConfiguration();

                Configuration.Configuration configuration = LoadConfiguration();

                ProcessPartnerApiPassword(configuration);
                ProcessKeycloakPassword(configuration);

                Log.Write("Configuration values:");

                LogConfigurationValues(configuration);

                return configuration;
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Error reading configuration", e);
            }
        }

        private static void ProcessPartnerApiPassword(Configuration.Configuration configuration)
        {
            if (configuration.IsPartnerApiPasswordEncrypted)
            {
                try
                {
                    configuration.PartnerApiPassword = EncryptionHelper.DecryptStringAES(configuration.PartnerApiPassword, configuration.PartnerApiUserName);
                    configuration.IsPartnerApiPasswordEncrypted = false;
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Could not decrypt PartnerApiPassword", e);
                }
            }
            else
            {
                string password = configuration.PartnerApiPassword;

                EncryptAndSavePartnerApiPasswordToConfiguration(configuration);

                configuration.PartnerApiPassword = password;
                configuration.IsPartnerApiPasswordEncrypted = false;
            }
        }

        private static void ProcessKeycloakPassword(Configuration.Configuration configuration)
        {
            if (configuration.IsKeycloakPasswordEncrypted)
            {
                try
                {
                    configuration.KeycloakPassword = EncryptionHelper.DecryptStringAES(configuration.KeycloakPassword, configuration.KeycloakUserName);
                    configuration.IsKeycloakPasswordEncrypted = false;
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Could not decrypt KeycloakPassword", e);
                }
            }
            else
            {
                string password = configuration.KeycloakPassword;

                EncryptAndSaveKeycloakPasswordToConfiguration(configuration);

                configuration.KeycloakPassword = password;
                configuration.IsKeycloakPasswordEncrypted = false;
            }
        }

        private static void LogConfigurationValues(Configuration.Configuration configuration)
        {
            string partnerApiPasswordKey = Utilities.GetMemberName((Configuration.Configuration c) => c.PartnerApiPassword);
            string keycloakPasswordKey = Utilities.GetMemberName((Configuration.Configuration c) => c.KeycloakPassword);

            foreach (var configurationValue in GetConfigurationValues(configuration))
            {
                string value = configurationValue.Value;

                if (configurationValue.Key == partnerApiPasswordKey || configurationValue.Key == keycloakPasswordKey)
                    value = new string('*', value.Length);

                Log.Write("  " + configurationValue.Key + " = " + value);
            }
        }

        private static void EncryptAndSavePartnerApiPasswordToConfiguration(Configuration.Configuration configuration)
        {
            string encryptedPassword = EncryptionHelper.EncryptStringAES(configuration.PartnerApiPassword, configuration.PartnerApiUserName);

            XDocument configurationDocument = GetConfigurationDocument();

            string partnerApiPasswordElementName = Utilities.GetMemberName((Configuration.Configuration c) => c.PartnerApiPassword);
            string isPartnerApiPasswordEncryptedName = Utilities.GetMemberName((Configuration.Configuration c) => c.IsPartnerApiPasswordEncrypted);

            UpdateConfigurationValue(configurationDocument, partnerApiPasswordElementName, encryptedPassword);
            UpdateConfigurationValue(configurationDocument, isPartnerApiPasswordEncryptedName, bool.TrueString.ToLower());

            SaveConfigurationDocument(configurationDocument);
        }

        private static void EncryptAndSaveKeycloakPasswordToConfiguration(Configuration.Configuration configuration)
        {
            string encryptedPassword = EncryptionHelper.EncryptStringAES(configuration.KeycloakPassword, configuration.KeycloakUserName);

            XDocument configurationDocument = GetConfigurationDocument();

            string keycloakPasswordElementName = Utilities.GetMemberName((Configuration.Configuration c) => c.KeycloakPassword);
            string isPartnerKeycloakEncryptedName = Utilities.GetMemberName((Configuration.Configuration c) => c.IsKeycloakPasswordEncrypted);

            UpdateConfigurationValue(configurationDocument, keycloakPasswordElementName, encryptedPassword);
            UpdateConfigurationValue(configurationDocument, isPartnerKeycloakEncryptedName, bool.TrueString.ToLower());

            SaveConfigurationDocument(configurationDocument);
        }

        private static XDocument GetConfigurationDocument()
        {
            string configurationXml = Utilities.LoadTextFile(FilePaths.ConfigurationFilePath);
            return XmlHelper.LoadToDocument(configurationXml);
        }

        private static void SaveConfigurationDocument(XDocument configurationDocument)
        {
            string xml = XmlHelper.SaveFromDocument(configurationDocument);
            Utilities.SaveTextFile(FilePaths.ConfigurationFilePath, xml);
        }

        private static void UpdateConfigurationValue(XDocument configurationDocument, string elementName, string value)
        {
            configurationDocument
                .Root
                .Element(typeof(Configuration.Configuration).Name)
                .Element(elementName)
                .Value = value;
        }

        private static Configuration.Configuration LoadConfiguration()
        {
            string configurationXml = Utilities.LoadTextFile(FilePaths.ConfigurationFilePath);
            string configurationXsd = Utilities.LoadEmbeddedResource(FilePaths.ConfigurationSchemaEmbeddedResourcePath);

            XmlHelper.Validate(configurationXml, configurationXsd);

            return XmlHelper.Deserialize<Configuration.EndeavourEmisWebPollerService>(configurationXml).Configuration;
        }

        private static Dictionary<string, string> GetConfigurationValues(Configuration.Configuration configuration)
        {
            return typeof(Configuration.Configuration)
                .GetFields()
                .ToDictionary(t => t.Name, t => t.GetValue(configuration).ToString());
        }

        private static void SaveNewConfiguration()
        {
            Log.Write("Configuration file not found, creating configuration file.");

            Utilities.EnsureDirectoryExists(FilePaths.ConfigurationPath);

            string configurationText = Utilities.LoadEmbeddedResource(FilePaths.ConfigurationEmbeddedResourcePath);

            Utilities.SaveTextFile(FilePaths.ConfigurationFilePath, configurationText);
        }
    }
}
