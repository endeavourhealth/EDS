using System;
using System.Runtime.Serialization;

namespace Endeavour.EmisWebPollerService.Keycloak
{
    [DataContract]
    public class AccessToken
    {
        [DataMember]
        public string access_token { get; set; }

        [DataMember]
        public long expires_in { get; set; }

        [DataMember]
        public string refresh_token { get; set; }

        private DateTime _expiration;

        public void SetExpiration()
        {
            _expiration = DateTime.Now.AddSeconds(expires_in);
        }

        public bool IsExpired()
        {
            return DateTime.Now > _expiration;
        }
    }
}
