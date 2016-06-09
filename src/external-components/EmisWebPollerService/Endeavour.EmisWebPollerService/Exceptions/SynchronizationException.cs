using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class SynchronizationException : PollerException
    {
        public SynchronizationException() : base()
        {
        }

        public SynchronizationException(string message) : base(message)
        {
        }

        public SynchronizationException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
