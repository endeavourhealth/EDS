using System;
using System.Collections.Specialized;
using System.IO;
using System.Net;
using System.Runtime.Serialization.Json;
using System.Web;

namespace Endeavour.EmisWebPollerService.Keycloak
{
    internal class KeycloakClient
    {
        private const string _tokenPath = "{0}/realms/{1}/protocol/openid-connect/token";

        private static KeycloakClient _instance;
        public static void Init(string baseUrl, string realm, string username, string password, string clientId)
        {
            _instance = new KeycloakClient(baseUrl, realm, username, password, clientId);
        }

        public static KeycloakClient Instance
        {
            get { return _instance; }
        }

        private readonly string _baseUrl;
        private readonly string _realm;
        private readonly string _username;
        private readonly string _password;
        private readonly string _clientId;
        private AccessToken _token;

        private KeycloakClient(string baseUrl, string realm, string username, string password, string clientId)
        {
            _baseUrl = baseUrl;
            _realm = realm;
            _username = username;
            _password = password;
            _clientId = clientId;
        }

        public string GetAuthorizationToken()
        {
            try
            {
                return "Bearer " + GetToken().access_token;
            }
            catch(Exception e)
            {
                throw new KeycloakException("Get authorization token failed.", e);
            }
        }

        private AccessToken GetToken()
        {
            if (_token == null)
                GetNewToken();
            else if (_token.IsExpired())
                RefreshToken();

            return _token;
        }

        private void GetNewToken()
        {
            NameValueCollection formData = HttpUtility.ParseQueryString(String.Empty);
            formData.Add("username", _username);
            formData.Add("password", _password);
            formData.Add("grant_type", "password");
            formData.Add("client_id", _clientId);

            _token = PerformRestCall(formData);
            _token.SetExpiration();
        }

        private void RefreshToken()
        {
            NameValueCollection formData = HttpUtility.ParseQueryString(String.Empty);
            formData.Add("grant_type", "refresh_token");
            formData.Add("client_id", _clientId);
            formData.Add("refresh_token", _token.refresh_token);

            _token = PerformRestCall(formData);
            _token.SetExpiration();
        }

        private AccessToken PerformRestCall(NameValueCollection formData)
        {
            using (HttpWebResponse httpWebResponse = Post(formData))
            {
                using (Stream responseStream = httpWebResponse.GetResponseStream())
                {
                    if (responseStream == null)
                        throw new KeycloakException("Invalid response from Keycloak service: ResponseStream is null.");

                    return (AccessToken)new DataContractJsonSerializer(typeof(AccessToken)).ReadObject(responseStream);
                }
            }
        }

        private HttpWebResponse Post(NameValueCollection formData)
        {
            string payloadAsString = formData.ToString();

            HttpWebRequest webRequest = (HttpWebRequest)WebRequest.Create(String.Format(_tokenPath, _baseUrl, _realm));
            webRequest.KeepAlive = false;
            webRequest.ProtocolVersion = HttpVersion.Version10;
            webRequest.Method = "POST";
            webRequest.ContentType = "application/x-www-form-urlencoded";

            using (var sw = new StreamWriter(webRequest.GetRequestStream()))
            {
                sw.Write(payloadAsString);
            }

            HttpWebResponse httpWebResponse = TryGetResponse(webRequest);

            return httpWebResponse;
        }

        private HttpWebResponse TryGetResponse(HttpWebRequest webRequest)
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
                throw new KeycloakException($"Keycloak Server returned {errorHttpWebResponse.StatusCode}", ex);
            }
        }
    }
}
