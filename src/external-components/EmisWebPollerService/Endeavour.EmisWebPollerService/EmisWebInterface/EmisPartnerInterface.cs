using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal sealed class EmisPartnerInterface
    {
        private static readonly Guid PatientAccessClassGuid = new Guid("{320C76CC-5FD0-4D58-A91E-7F03E3989F8B}");
        private static readonly Guid PatientAccessInterfaceGuid = new Guid("{5DA607CA-8533-44AB-81F6-860F421E6378}");

        private object _patientAccess;

        public EmisPartnerInterface()
        {
            Initialise();
        }

        public InitialiseWithIdResult InitializeWithId(string ipAddress, int organisationId, string partnerApiKey)
        {
            const string MethodName = "InitializeWithID";
            
            object cdb = null;
            object productName = null;
            object version = null;
            object loginID = null; 
            object error = null;
            object outcome = null;
            object sessionId = null;

            const int outParameterStartIndex = 6;

            object[] parameters = new object[]
            {
                // in
                2,
                ipAddress,
                string.Empty,
                string.Empty,
                organisationId,
                partnerApiKey,

                // out
                cdb,
                productName,
                version,
                loginID,
                error,
                outcome,
                sessionId
            };

            ParameterModifier[] modifiers = CreateParameterModifiers(parameters.Length, outParameterStartIndex);

            InvokeMethod(MethodName, parameters, modifiers);

            int index = outParameterStartIndex;

            InitialiseWithIdResult result = new InitialiseWithIdResult()
            {
                Cdb = parameters.GetAs<object>(index++).WhenNotNull(t => t.ToString()),
                ProductName = parameters.GetAs<string>(index++),
                Version = parameters.GetAs<string>(index++),
                LoginId = parameters.GetAs<string>(index++),
                Error = parameters.GetAs<string>(index++),
                Outcome = parameters.GetAs<object>(index++).WhenNotNull(t => t.ToString()),  // type varies!
                SessionId = parameters.GetAs<string>(index++),
            };

            if (result.IsError)
                throw new PartnerApiException(string.Format("Partner API {0} call failed: {1} (Version = {2})", MethodName, result.ErrorMessage, result.Version));

            return result;
        }

        public LogonResult Logon(string loginId, string userName, string password)
        {
            const string MethodName = "Logon";

            object sessionId = null;
            object error = null;
            object outcome = null;

            const int outParameterStartIndex = 3;

            object[] parameters = new object[]
            {
                // in
                loginId,  
                userName,
                password,

                // out
                sessionId,
                error,
                outcome
            };

            ParameterModifier[] modifiers = CreateParameterModifiers(parameters.Length, outParameterStartIndex);

            InvokeMethod(MethodName, parameters, modifiers);

            int index = outParameterStartIndex;

            LogonResult result = new LogonResult()
            {
                SessionId = parameters.GetAs<string>(index++),
                Error = parameters.GetAs<string>(index++),
                Outcome = parameters.GetAs<object>(index++).WhenNotNull(t => t.ToString())  // type varies!
            };

            if (result.IsError)
                throw CreatePartnerApiException(MethodName, result);

            return result;
        }

        public PatientMatchesResult GetPatientSequence(string sessionId, int startId, int batchSize)
        {
            const string MethodName = "GetPatientSequence";

            object[] parameters = new object[]
            {
                sessionId,
                startId,
                batchSize,
            };

            PatientMatchesResult result = InvokeMethodWithSingleXmlResult<PatientMatchesResult>(MethodName, parameters);

            if (result.IsError)
                throw CreatePartnerApiException(MethodName, result);

            return result;
        }

        public GetMedicalRecordResult GetMedicalRecord(string sessionId, int patientId)
        {
            const string MethodName = "GetMedicalRecord";

            object[] parameters = new object[]
            {
                sessionId,
                patientId,
            };

            GetMedicalRecordResult result = InvokeMethodWithSingleXmlResult<GetMedicalRecordResult>(MethodName, parameters);

            if (result.IsError)
                throw CreatePartnerApiException(MethodName, result);

            return result;
        }

        public PatientMatchesResult GetChangedPatients(string sessionId, DateTime fromDate)
        {
            const string MethodName = "GetChangedPatients";

            object[] parameters = new object[]
            {
                sessionId,
                fromDate.ToString("dd/MM/yyyy")
            };

            PatientMatchesResult result = InvokeMethodWithSingleXmlResult<PatientMatchesResult>(MethodName, parameters);

            if (result.IsError)
                throw CreatePartnerApiException(MethodName, result);

            return result;
        }

        public PatientMatchesResult GetChangedPatientsMR(string sessionId, DateTime fromDate)
        {
            const string MethodName = "GetChangedPatientsMR";

            object[] parameters = new object[]
            {
                sessionId,
                fromDate.ToString("dd/MM/yyyy")
            };

            PatientMatchesResult result = InvokeMethodWithSingleXmlResult<PatientMatchesResult>(MethodName, parameters);

            if (result.IsError)
                throw CreatePartnerApiException(MethodName, result);

            return result;
        }

        public XmlResult GetMatchedPatient(string sessionId, string matchedTerms)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetBookedPatients(string sessionId, int minutesWithin, int minutesBefore)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetAttachments(string sessionId, int patientId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetBase64AttachmentData(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetOrganisation(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult FileRecord(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetPatientDemographics(string sessionId, int patientId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetCurrentPatientId(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetCurrentUser(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetCodedRecord(string sessionId, int patientId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetEvents(string sessionId, int patientId, int numberOfDays)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetArrived(string sessionId, int minutesWithin, int minutesBefore, int appSite)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetSentIn(string sessionId, int minutesWithin, int minutesBefore, int appSite)
        {
            throw new NotImplementedException();
        }

        public XmlResult SetAppointmentStatus(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetAppointmentConfiguration(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetUserById(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetAppointmentSessions(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetSlotsForSession(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult BookAppointment(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult CancelAppointment(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetPatientAppointments(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult SetSlotType(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult SetAppointmentStatusEx(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult SwapPatient(string sessionId)
        {
            throw new NotImplementedException();
        }

        public XmlResult GetPatientSearches(string sessionId)
        {
            throw new NotImplementedException();
        }

        #region Helper methods

        private void Initialise()
        {
            Type patientAccessType = Type.GetTypeFromCLSID(PatientAccessClassGuid);
            object patientAccessObject = Activator.CreateInstance(patientAccessType);

            _patientAccess = GetManagedObjectByInterface(patientAccessObject, PatientAccessInterfaceGuid);
        }

        private XmlResult InvokeMethodWithSingleXmlResult(string methodName, object[] inputParameters)
        {
            return InvokeMethodWithSingleXmlResult<XmlResult>(methodName, inputParameters);
        }
        
        private T InvokeMethodWithSingleXmlResult<T>(string methodName, object[] inputParameters) where T : XmlResult, new()
        {
            object resultXml = null;
            object error = null;
            object outcome = null;

            object[] outputParameters = new object[]
            {
                resultXml,
                error,
                outcome
            };

            object[] parameters = inputParameters.Concat(outputParameters).ToArray();
            int outputParametersStartIndex = inputParameters.Length;

            ParameterModifier[] modifiers = CreateParameterModifiers(parameters.Length, outputParametersStartIndex);

            InvokeMethod(methodName, parameters, modifiers);

            int index = outputParametersStartIndex;

            return new T()
            {
                ResultXml = parameters.GetAs<string>(index++),
                Error = parameters.GetAs<string>(index++),
                Outcome = parameters[index++].WhenNotNull(t => t.ToString()),  // type varies!
            };
        }

        private object InvokeMethod(string methodName, object[] arguments, ParameterModifier[] modifiers)
        {
            return _patientAccess.GetType().InvokeMember(methodName, BindingFlags.InvokeMethod, null, _patientAccess, arguments, modifiers, null, null);
        }

        private static PartnerApiException CreatePartnerApiException(string methodName, Result result)
        {
            return new PartnerApiException(string.Format("Partner API {0} call failed: {1}", methodName, result.ErrorMessage));
        }

        private static ParameterModifier[] CreateParameterModifiers(int length, int outParameterStartIndex)
        {
            ParameterModifier p = new ParameterModifier(length);

            for (int i = outParameterStartIndex; i < length; i++)
                p[i] = true;

            return new ParameterModifier[] { p };
        }

        private static object GetManagedObjectByInterface(object comObject, Guid interfaceGuid)
        {
            IntPtr iUnknownPointer = Marshal.GetIUnknownForObject(comObject);

            try
            {
                object interfaceObject = null;
                IntPtr targetObjectPointer;

                int hResult = Marshal.QueryInterface(iUnknownPointer, ref interfaceGuid, out targetObjectPointer);

                if (hResult == 0)
                {
                    interfaceObject = Marshal.GetObjectForIUnknown(targetObjectPointer);
                    Marshal.Release(targetObjectPointer);
                }

                return interfaceObject;
            }
            finally
            {
                Marshal.Release(iUnknownPointer);
            }
        }

        #endregion
    }
}
