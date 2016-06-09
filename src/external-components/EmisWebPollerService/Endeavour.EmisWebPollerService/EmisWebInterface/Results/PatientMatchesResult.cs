using Endeavour.EmisWebPollerService.EomPatientMatches36;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class PatientMatchesResult : XmlResult
    {
        public PatientMatchesResult()
        {
            _successCodes.Add("1", "Found matches");
            _successCodes.Add("3", "No patients");

            _errorCodes.Add("-1", "Technical error");
            _errorCodes.Add("2", "Access denied");
        }
        
        public PatientMatches PatientMatches
        {
            get
            {
                return XmlHelper.Deserialize<PatientMatches>(ResultXml);
            }
        }

        public int[] PatientIds
        {
            get
            {
                if (!HasMatches)
                    return new int[] { };
                
                return PatientMatches
                    .PatientList
                    .Select(t => int.Parse(t.DBID))
                    .ToArray();
            }
        }

        public bool HasMatches
        {
            get
            {
                return (!string.IsNullOrWhiteSpace(ResultXml));
            }
        }
    }
}
