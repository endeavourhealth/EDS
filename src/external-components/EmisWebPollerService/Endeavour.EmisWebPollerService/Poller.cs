using System;
using System.Collections.Generic;
using System.Linq;

namespace Endeavour.EmisWebPollerService
{
    internal class Poller : PollerBase
    {
        private EmisPartnerInterface _emisInterface;
        private string _sessionId;
        private StateManager _state;

        public Poller(string serviceName, Action requestServiceStopCallback) : base(serviceName, requestServiceStopCallback)
        {
            
        }

        internal override void DoWork()
        {
            try
            {
                Log.Write("Starting synchronization");

                _state = StateManager.Instance;

                if (_emisInterface == null)
                    _emisInterface = new EmisPartnerInterface();

                if (_stopping)
                    return;
                
                InitialiseApi();

                if (_state.Mode == State.Mode.BULK)
                {
                    if (!_state.BulkInitialised)
                    {
                        _state.StartBulk();

                        int[] patientIds = GetBulkPatientList();

                        if (_stopping)
                            return;

                        _state.AddBulkPatients(patientIds);
                    }

                    ProcessQueue();

                    _state.CompleteBulk();
                }
                else if (_state.Mode == State.Mode.TRANSACTIONAL)
                {
                    DateTime synchronisationStart = DateTime.UtcNow;

                    int[] changedPatients = GetChangedPatients(_state.LastSynchronisation.Value);

                    if (_stopping)
                        return;

                    _state.AddChangedPatients(changedPatients, synchronisationStart);

                    ProcessQueue();
                }
                else
                {
                    throw new NotSupportedException("Unrecognised mode");
                }
                
                Log.Write("Synchronization completed");
            }
            catch (Exception e)
            {
                Log.Write("Synchronization stopped, error synchronising.", e);
            }
        }

        private void ProcessQueue()
        {
            while (!_state.IsQueueEmpty)
            {
                if (_stopping)
                    return;
                
                int nextPatientId = _state.GetNextPatient();

                string patientRecord = GetPatientRecord(nextPatientId);

                SendToEndeavourDataService(nextPatientId, patientRecord);

                _state.RemovePatient(nextPatientId);
            }
        }

        private void InitialiseApi()
        {
            InitialiseWithIdResult initialiseResult = _emisInterface.InitializeWithId(_configuration.EmisWebIPAddress, (int)_configuration.EmisWebCdb, _configuration.PartnerApiKey);

            if (_stopping)
                return;
            
            LogonResult logonResult = _emisInterface.Logon(initialiseResult.LoginId, _configuration.PartnerApiUserName, _configuration.PartnerApiPassword);

            _sessionId = logonResult.SessionId;
        }

        private int[] GetChangedPatients(DateTime since)
        {
            PatientMatchesResult result = _emisInterface.GetChangedPatients(_sessionId, since);

            if (_stopping)
                return null;

            PatientMatchesResult mrResult = _emisInterface.GetChangedPatientsMR(_sessionId, since);

            return result
                .PatientIds
                .Concat(mrResult.PatientIds)
                .Distinct()
                .ToArray();
        }

        private string GetPatientRecord(int patientId)
        {
            GetMedicalRecordResult result =_emisInterface.GetMedicalRecord(_sessionId, patientId);
            return result.ResultXml;
        }

        private int[] GetBulkPatientList()
        {
            const int PatientBatchSize = 100;

            bool hasMatches = true;
            int nextPatientId = 0;
            List<int> patientIds = new List<int>();

            while (hasMatches)
            {
                if (_stopping)
                    return null;
                
                PatientMatchesResult result = _emisInterface.GetPatientSequence(_sessionId, nextPatientId, PatientBatchSize);

                hasMatches = result.HasMatches;

                if (hasMatches)
                {
                    int[] patientIdsBatch = result
                        .PatientIds
                        .OrderBy(t => t)
                        .ToArray();

                    patientIds.AddRange(patientIdsBatch);

                    nextPatientId = patientIdsBatch.Last();
                }
            }

            return patientIds.ToArray();
        }

        private void SendToEndeavourDataService(int patientId, string patientXml)
        {
            ServerApi.Send(_configuration.EndeavourServiceEndpoint,
                patientId,
                _configuration.OdsCode,
                patientXml);
            Log.Write($"Patient Sent: {patientId}");
        }
    }
}
