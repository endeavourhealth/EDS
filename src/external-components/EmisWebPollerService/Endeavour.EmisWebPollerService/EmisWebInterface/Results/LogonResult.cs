using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class LogonResult : Result
    {
        public LogonResult()
        {
            _successCodes.Add("1", "Successful");
            
            _errorCodes.Add("-1", "Technical error");
            _errorCodes.Add("2", "Expired");
            _errorCodes.Add("3", "Unsuccessful");
            _errorCodes.Add("4", "Invalid login ID or login ID does not have access to this product");
        }
        
        public string SessionId { get; set; }
    }
}
