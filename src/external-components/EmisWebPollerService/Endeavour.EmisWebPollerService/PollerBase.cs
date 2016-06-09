using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal abstract class PollerBase
    {
        private string _serviceName;
        private Action _requestServiceStopCallback;
        private Thread _thread;
        protected volatile bool _stopping = false;
        protected Configuration.Configuration _configuration;
        private DateTime _lastSynchronization = DateTime.MinValue;
        private const int ServiceThreadSleepMillisecondsDefault = 1 * 1000;
        private const int SynchronizationFrequencyMinutesDefault = 15;

        public PollerBase(string serviceName, Action requestServiceStopCallback)
        {
            _serviceName = serviceName;
            _requestServiceStopCallback = requestServiceStopCallback;
            _thread = new Thread(Run);
            Log.RegisterServiceName(_serviceName);
        }

        public void Start()
        {
            Log.WriteLogDivider();
            Log.Write("Starting service '{0}'.", _serviceName);

            _thread.Start();
        }

        public bool Stop()
        {
            Log.Write("Stopping service '{0}'.", _serviceName);
            
            _stopping = true;

            return _thread.Join(10 * 1000);
        }

        public void Run()
        {
            Thread.Sleep(10000);

            try
            {
                WriteStartupLog();
                ReadConfiguration();

                while (!_stopping)
                {
                    if ((_lastSynchronization.AddMinutes(GetSynchronizationFrequencyMinutes(_configuration)) < DateTime.Now))
                    {
                        DoWork();
                        _lastSynchronization = DateTime.Now;
                    }

                    if (!_stopping)
                        Thread.Sleep(GetServiceThreadSleepMilliseconds(_configuration));
                }
            }
            catch (Exception e)
            {
                Log.Write("Fatal exception occurred.", true);
                Log.Write(e.ToFormattedString(), true);
                Log.Write("Stopping service '{0}' due to fatal exception.", true, _serviceName);
                
                StopServiceNonBlocking();

                while (!_stopping)
                    Thread.Sleep(GetServiceThreadSleepMilliseconds(_configuration));
            }

            try
            {
                WriteShutdownLog();
            }
            catch
            {
                // do nothing
            }
        }

        internal abstract void DoWork();

        private void StopServiceNonBlocking()
        {
            new Thread(new ThreadStart(() => 
            {
                try
                {
                    _requestServiceStopCallback();
                }
                catch
                {
                    // do nothing
                }
            })).Start();
        }

        private void WriteStartupLog()
        {
            Log.Write("Started service '{0}'.", _serviceName);
            Log.Write("Executing from '{0}'.", Assembly.GetExecutingAssembly().Location);
            Log.Write("Running service as '{0}' on machine '{1}' with operating system '{2}'.", Environment.UserDomainName, Environment.MachineName, Environment.OSVersion.ToString());
            Log.Write("Writing log to '{0}'.", FilePaths.LogPath);
        }

        private void WriteShutdownLog()
        {
            Log.Write("Stopped service '{0}'.", _serviceName);
        }

        private void ReadConfiguration()
        {
            _configuration = ConfigurationManager.ReadConfiguration();
        }

        public static int GetSynchronizationFrequencyMinutes(Configuration.Configuration configuration)
        {
            int minutes = SynchronizationFrequencyMinutesDefault;

            if (configuration != null)
                minutes = configuration.SynchronizationFrequencyMinutes;

            return minutes;
        }

        public static int GetServiceThreadSleepMilliseconds(Configuration.Configuration configuration)
        {
            int minutes = ServiceThreadSleepMillisecondsDefault;

            if (configuration != null)
                minutes = configuration.SynchronizationFrequencyMinutes;

            return minutes;
        }

    }
}
