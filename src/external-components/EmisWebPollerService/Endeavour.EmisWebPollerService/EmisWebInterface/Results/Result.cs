using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal abstract class Result
    {
        protected Dictionary<string, string> _successCodes = new Dictionary<string, string>();
        protected Dictionary<string, string> _errorCodes = new Dictionary<string, string>();
        
        public string Error { get; set; }
        public string Outcome { get; set; }

        public bool IsError
        {
            get
            {
                if (_successCodes.Count > 0)
                    if (!_successCodes.ContainsKey(Outcome))
                        return true;

                return (!string.IsNullOrWhiteSpace(Error));
            }
        }

        public string ErrorMessage
        {
            get
            {
                if (IsError)
                {
                    string errorMessage = Error;

                    if (string.IsNullOrWhiteSpace(errorMessage))
                    {
                        if (_errorCodes.ContainsKey(Outcome))
                            errorMessage = _errorCodes[Outcome];
                        else
                            errorMessage = "Unknown error";
                    }

                    return string.Format("{0} (code {1})", errorMessage, Outcome);
                }
                else
                {
                    return string.Empty;
                }
            }
        }
    }
}
