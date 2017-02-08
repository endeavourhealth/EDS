using System;

namespace Endeavour.EmisWebPollerService
{
    internal class ServerApiException : PollerException
    {
        public ServerApiException() : base()
        {
        }

        public ServerApiException(string message) : base(message)
        {
        }

        public ServerApiException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
