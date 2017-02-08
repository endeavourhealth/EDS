using System;

namespace Endeavour.EmisWebPollerService
{
    internal class KeycloakException : PollerException
    {
        public KeycloakException() : base()
        {
        }

        public KeycloakException(string message) : base(message)
        {
        }

        public KeycloakException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
