using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal abstract class PollerException : Exception
    {
        public PollerException() : base()
        {
        }

        public PollerException(string message) : base(message)
        {
        }

        public PollerException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
