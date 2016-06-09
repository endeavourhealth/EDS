using Endeavour.EmisWebPollerService.EomMedicalRecord38;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class GetMedicalRecordResult : XmlResult
    {
        public GetMedicalRecordResult()
        {
            _successCodes.Add("1", "Access allowed");
            
            _errorCodes.Add("2", "Access denied");
            _errorCodes.Add("3", "Patient not found");
            _errorCodes.Add("4", "Record is restricted");
        }

        public MedicalRecordType MedicalRecord
        {
            get
            {
                return XmlHelper.Deserialize<MedicalRecordType>(this.ResultXml);
            }
        }
    }
}
