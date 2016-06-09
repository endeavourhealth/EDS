using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal sealed class ConsoleServiceWrapper
    {
        private volatile bool _requestStop = false;
        private Poller _poller;
        
        public void Run()
        {
            const string ServiceName = "EndeavourEmisWebPollerConsole";

            _poller = new Poller(ServiceName, Stop);
            _poller.Start();

            Log.WriteToConsoleAndLog(string.Empty);
            Log.WriteToConsoleAndLog(ServiceName + " service running.  Press any key to stop service.");

            while (true)
            {
                if (_requestStop)
                    break;

                if (Console.KeyAvailable)
                {
                    while (Console.KeyAvailable)
                        Console.Read();

                    Log.WriteToConsoleAndLog("Key pressed.  Stopping service...");
                    break;
                }

                Thread.Sleep(200);
            }

            _poller.Stop();

            Log.WriteToConsoleAndLog("Service stopped.");
        }

        public void Stop()
        {
            _requestStop = true;
        }
    }
}
