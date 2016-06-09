using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal static class FilePaths
    {
        private static readonly string CommonApplicationDataPath = Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData);
        private static readonly string AssemblyName = Assembly.GetExecutingAssembly().GetName().Name;
        private readonly static string ApplicationDataPath = CommonApplicationDataPath + @"\Endeavour Health\EmisWebPollerService";
        public readonly static string ConfigurationPath = ApplicationDataPath + @"\Configuration";
        public readonly static string StatePath = ApplicationDataPath + @"\State";
        public readonly static string LogPath = ApplicationDataPath + @"\Log";
        public readonly static string ConfigurationEmbeddedResourcePath = AssemblyName + ".Configuration.Configuration.xml";
        public readonly static string ConfigurationSchemaEmbeddedResourcePath = AssemblyName + ".Configuration.Configuration.xsd";
        public readonly static string ConfigurationFilePath = ConfigurationPath + @"\Configuration.xml";
        public readonly static string StateEmbeddedResourcePath = AssemblyName + ".Configuration.State.xml";
        public readonly static string StateSchemaEmbeddedResourcePath = AssemblyName + ".Configuration.State.xsd";
        public readonly static string StateFilePath = StatePath + @"\State.xml";
    }
}
