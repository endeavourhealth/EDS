using Endeavour.EmisWebPollerService.Keycloak;
using System;
using System.IO;
using System.Net;

namespace Endeavour.EmisWebPollerService
{
    internal static class ServerApi
    {
        private const string MessageHeaderTemplate = @"<?xml version=""1.0"" encoding=""UTF-8""?>
<Bundle xmlns = ""http://hl7.org/fhir"" >
   <type value=""collection"" />
   <entry>
      <resource>
         <MessageHeader>
            <id value=""{0}"" />
            <timestamp value=""{1:o}"" />
            <source>
               <name value=""{2}"" />
               <software value=""EMISOPEN"" />
               <version value=""1.0"" />
            </source>
            <data>
               <reference value=""Binary/{3}"" />
            </data>
         </MessageHeader>
      </resource>
   </entry>
   <entry>
      <resource>
         <Binary>
            <id value=""{3}"" />
            <contentType value=""text/xml""/>
            <content value=""{4}"" />
         </Binary>
      </resource>
   </entry>
</Bundle>";

        public static void Send(string url, int patientId, string odsCode, string data)
        {
            using (HttpWebResponse httpWebResponse = Post(url, patientId, odsCode, data))
            {
                httpWebResponse.Close();
            }
        }

        private static HttpWebResponse Post(string url, int patientId, string odsCode, string data)
        {
            string payloadAsString = CreatePayload(patientId, odsCode, data);

            HttpWebRequest webRequest = (HttpWebRequest)WebRequest.Create(url);
            webRequest.Method = "POST";
            webRequest.ContentType = "text/xml";
            webRequest.Headers.Add("Authorization", KeycloakClient.Instance.GetAuthorizationToken());

            using (var sw = new StreamWriter(webRequest.GetRequestStream()))
            {
                sw.Write(payloadAsString);
            }

            HttpWebResponse httpWebResponse = TryGetResponse(webRequest);

            return httpWebResponse;
        }

        private static HttpWebResponse TryGetResponse(HttpWebRequest webRequest)
        {
            try
            {
                return webRequest.GetResponse() as HttpWebResponse;
            }
            catch (WebException ex)
            {
                if (ex.Response == null)
                    throw;

                var errorHttpWebResponse = ex.Response as HttpWebResponse;

                switch (errorHttpWebResponse.StatusCode)
                {
                    case HttpStatusCode.InternalServerError:
                        throw new ServerApiException("Server Api experienced a problem", ex);
                    case HttpStatusCode.NotFound:
                        throw new ServerApiException("Can't connect to Server Api", ex);
                    case HttpStatusCode.BadRequest:
                    case HttpStatusCode.Unauthorized:
                        throw new ServerApiException("Security tokens are invalid", ex);
                    default:
                        throw new ServerApiException($"Server Api returned {errorHttpWebResponse.StatusCode}", ex);
                }
            }
        }

        private static string CreatePayload(int patientId, string odsCode, string payload)
        {
            var messageId = Guid.NewGuid();
            var timeStamp = DateTime.UtcNow;
            var encodedPayload = Utilities.EncodeToBase64String(payload);

            return string.Format(MessageHeaderTemplate,
                messageId,
                timeStamp,
                odsCode,
                patientId,
                encodedPayload);
        }
    }
}
