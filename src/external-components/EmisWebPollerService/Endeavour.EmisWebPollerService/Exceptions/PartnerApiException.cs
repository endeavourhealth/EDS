using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class PartnerApiException : Exception
    {
        public PartnerApiException() : base()
        {
        }

        public PartnerApiException(string message) : base(message)
        {
        }

        public PartnerApiException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
