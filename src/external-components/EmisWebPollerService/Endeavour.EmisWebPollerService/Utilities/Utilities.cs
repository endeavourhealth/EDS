using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Net;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal static class Utilities
    {
        public static void EnsureDirectoryExists(string path)
        {
            if (!Directory.Exists(path))
                Directory.CreateDirectory(path);
        }

        public static string LoadEmbeddedResource(string resourceName)
        {
            using (Stream stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
                using (StreamReader reader = new StreamReader(stream))
                    return reader.ReadToEnd();
        }

        public static string LoadTextFile(string filePath)
        {
            return File.ReadAllText(filePath);
        }

        public static void SaveTextFile(string filePath, string text)
        {
            Utilities.EnsureDirectoryExists(Path.GetDirectoryName(filePath));
            
            File.WriteAllText(filePath, text);
        }

        public static void AppendToTextFile(string filePath, string text)
        {
            Utilities.EnsureDirectoryExists(Path.GetDirectoryName(filePath));
            
            File.AppendAllText(filePath, text);
        }

        public static string ToFormattedString(this Exception e)
        {
            string message = "Exception: " + e.GetType().Name + Environment.NewLine
                   + "Message: " + e.Message + Environment.NewLine
                   + "Stack Trace: " + Environment.NewLine + Environment.NewLine + e.StackTrace;

            if (e.InnerException != null)
                message += Environment.NewLine + Environment.NewLine + "Inner " + e.InnerException.ToFormattedString();

            return message;
        }

        public static int? ParseNullableInt(string value)
        {
            if (string.IsNullOrEmpty(value))
                return null;

            return int.Parse(value);
        }

        public static bool IsValidUrl(string url)
        {
            Uri uriResult;
            return Uri.TryCreate(url, UriKind.Absolute, out uriResult) && (uriResult.Scheme == Uri.UriSchemeHttp || uriResult.Scheme == Uri.UriSchemeHttps);
        }

        public static string GetMemberName<T, U>(Expression<Func<T, U>> expression)
        {
            var member = expression.Body as MemberExpression;

            if (member != null)
                return member.Member.Name;

            throw new ArgumentException("Expression is not a member access", "expression");
        }

        public static HttpWebResponse HttpPost(string url, string data)
        {
            byte[] bytes = System.Text.Encoding.UTF8.GetBytes(data);

            WebRequest request = WebRequest.Create(url);
            request.Method = "POST";

            request.ContentLength = bytes.Length;

            using (Stream contentStream = request.GetRequestStream())
                contentStream.Write(bytes, 0, bytes.Length);

            HttpWebResponse response = null;

            try
            {
                response = (HttpWebResponse)request.GetResponse();
            }
            catch (WebException e)
            {
                response = e.Response as HttpWebResponse;

                if (response == null)
                    throw;
            }

            return response;
        }
    }
}
