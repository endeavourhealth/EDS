using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class InitialiseWithIdResult : Result
    {
        public InitialiseWithIdResult()
        {
            _successCodes.Add("1", "Successful initialise awaiting logon");
            _successCodes.Add("4", "Auto logon successful");

            _errorCodes.Add("-1", "Refer to error");
            _errorCodes.Add("2", "Unable to connect to server due to absent server, or incorrect details");
            _errorCodes.Add("3", "Unmatched supplier ID");
            _errorCodes.Add("4", "Invalid login ID or login ID does not have access to this product");
        }
        
        public string Cdb { get; set; }
        public string ProductName { get; set; }
        public string Version { get; set; }
        public string LoginId { get; set; }
        public string SessionId { get; set; }
    }
}
