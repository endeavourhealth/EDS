using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class ConfigurationException : PollerException
    {
        public ConfigurationException() : base()
        {
        }

        public ConfigurationException(string message) : base(message)
        {
        }

        public ConfigurationException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
