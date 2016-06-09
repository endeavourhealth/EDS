using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Schema;
using System.Xml.Serialization;

namespace Endeavour.EmisWebPollerService
{
    internal static class XmlHelper
    {
        public static T Deserialize<T>(string xml) where T : new()
        {
            XmlSerializer serializer = new XmlSerializer(typeof(T));
            
            using (StringReader reader = new StringReader(xml))
                return (T)serializer.Deserialize(reader);
        }

        public static string Serialize<T>(T obj) where T : new()
        {
            XmlSerializer serializer = new XmlSerializer(typeof(T));

            using (StringWriter writer = new StringWriter())
            {
                serializer.Serialize(writer, obj);
                return writer.ToString();
            }
        }

        public static XDocument LoadToDocument(string xml)
        {
            using (StringReader reader = new StringReader(xml))
                return XDocument.Load(reader, LoadOptions.PreserveWhitespace);
        }

        public static string SaveFromDocument(XDocument document)
        {
            return document.ToString(SaveOptions.DisableFormatting);
        }

        public static void Validate(string xml, params string[] xsds)
        {
            XDocument xmlDocument;

            using (StringReader reader = new StringReader(xml))
                xmlDocument = XDocument.Load(reader);

            List<XmlSchema> xmlSchemas = new List<XmlSchema>();

            XmlSchemaSet schemas = new XmlSchemaSet();
            
            foreach (string xsd in xsds)
                using (StringReader reader = new StringReader(xsd))
                    schemas.Add(XmlSchema.Read(reader, (sender, e) => { throw new XmlSchemaValidationException(e.Message); }));

            xmlDocument.Validate(schemas, null);
        }
    }
}
