using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal class StateManager
    {
        private static StateManager _instance = null;

        public static StateManager Instance
        {
            get
            {
                if (_instance == null)
                    _instance = new StateManager();

                return _instance;
            }
        }

        private State.State _state = null;
        private bool _preventSave = false;

        private StateManager()
        {
            ReadState();
        }

        public void ClearQueue()
        {
            _state.Queue = new int[] { };
            
            SaveState();
        }
        
        public void AddPatientsToQueue(int[] patientIds)
        {
            _state.Queue = _state
                .Queue
                .Concat(patientIds)
                .ToArray();

            SaveState();
        }

        public bool IsQueueEmpty
        {
            get
            {
                return (_state.Queue.Length == 0);
            }
        }

        public int GetNextPatient()
        {
            return _state.Queue.First();
        }

        public void RemovePatient(int patientId)
        {
            _state.Queue = _state
                .Queue
                .Where(t => t != patientId)
                .ToArray();

            SaveState();
        }

        public void SetMode(State.Mode mode)
        {
            _state.Mode = mode;
            
            SaveState();
        }

        public void StartBulk()
        {
            PerformSinglePersistedAction(() =>
            {
                BulkInitialised = false;
                BulkStarted = DateTime.UtcNow;
                BulkCompleted = null;
                LastSynchronisation = null;
                ClearQueue();
            });
        }

        public void AddBulkPatients(int[] patientIds)
        {
            PerformSinglePersistedAction(() =>
            {
                AddPatientsToQueue(patientIds);
                BulkInitialised = true;
            });
        }

        public void AddChangedPatients(int[] patientIds, DateTime sychronisationStart)
        {
            PerformSinglePersistedAction(() =>
            {
                AddPatientsToQueue(patientIds);
                LastSynchronisation = sychronisationStart;
            });
        }

        public void CompleteBulk()
        {
            PerformSinglePersistedAction(() =>
            {
                LastSynchronisation = (DateTime)BulkStarted;
                BulkCompleted = DateTime.UtcNow;
                Mode = State.Mode.TRANSACTIONAL;
            });
        }

        private void PerformSinglePersistedAction(Action action)
        {
            _preventSave = true;

            try
            {
                action();
            }
            finally
            {
                _preventSave = false;
            }

            SaveState();
        }

        public bool BulkInitialised
        {
            get
            {
                return _state.BulkInitialised;
            }
            set
            {
                _state.BulkInitialised = true;

                SaveState();
            }
        }

        public State.Mode Mode
        {
            get
            {
                return _state.Mode;
            }
            set
            {
                _state.Mode = value;
                
                SaveState();
            }
        }

        public DateTime? LastSynchronisation
        {
            get
            {
                return _state.LastSynchronisation.DateSpecified ? _state.LastSynchronisation.Date : (DateTime?)null;
            }
            set
            {
                _state.LastSynchronisation.DateSpecified = (value != null);
                
                if (value != null)
                    _state.LastSynchronisation.Date = (DateTime)value;

                SaveState();
            }
        }

        public DateTime? BulkStarted
        {
            get
            {
                return _state.BulkStarted.DateSpecified ? _state.BulkStarted.Date : (DateTime?)null;
            }
            set
            {
                _state.BulkStarted.DateSpecified = (value != null);

                if (value != null)
                    _state.BulkStarted.Date = (DateTime)value;

                SaveState();
            }
        }

        public DateTime? BulkCompleted
        {
            get
            {
                return _state.BulkCompleted.DateSpecified ? _state.BulkCompleted.Date : (DateTime?)null;
            }
            set
            {
                _state.BulkCompleted.DateSpecified = (value != null);

                if (value != null)
                    _state.BulkCompleted.Date = (DateTime)value;

                SaveState();
            }
        }

        private void ReadState()
        {
            try
            {
                Log.Write("Reading state from '{0}'.", FilePaths.StateFilePath);

                if (!File.Exists(FilePaths.StateFilePath))
                    CreateNewStateFile();

                _state = LoadState();

                Log.Write("State values:");

                LogStateValues();
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Error reading state", e);
            }
        }

        private static State.State LoadState()
        {
            string configurationXml = Utilities.LoadTextFile(FilePaths.StateFilePath);
            string configurationXsd = Utilities.LoadEmbeddedResource(FilePaths.StateSchemaEmbeddedResourcePath);

            XmlHelper.Validate(configurationXml, configurationXsd);

            return XmlHelper.Deserialize<State.EndeavourEmisWebPollerService>(configurationXml).State;
        }

        private void SaveState()
        {
            if (_preventSave)
                return;

            State.EndeavourEmisWebPollerService pollerService = new State.EndeavourEmisWebPollerService()
            {
                State = _state
            };
            
            string stateXml = XmlHelper.Serialize<State.EndeavourEmisWebPollerService>(pollerService);
            Utilities.SaveTextFile(FilePaths.StateFilePath, stateXml);
        }

        private static void CreateNewStateFile()
        {
            Log.Write("State file not found, creating state file.");

            Utilities.EnsureDirectoryExists(FilePaths.StatePath);

            string stateXml = Utilities.LoadEmbeddedResource(FilePaths.StateEmbeddedResourcePath);

            Utilities.SaveTextFile(FilePaths.StateFilePath, stateXml);
        }

        private void LogStateValues()
        {
            Log.Write("  Mode = " + _state.Mode.ToString());
            
            string lastSyncDate = "(not set)";

            if (_state.LastSynchronisation.DateSpecified)
                lastSyncDate = _state.LastSynchronisation.Date.ToString("yyyy-MMM-dd HH:mm:ss");

            Log.Write("  Last sync date = " + lastSyncDate);
            Log.Write("  Patients in queue = " + _state.Queue.Length);
        }
    }
}
