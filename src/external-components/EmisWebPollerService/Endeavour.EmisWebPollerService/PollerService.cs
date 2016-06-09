using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.ServiceProcess;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal partial class PollerService : ServiceBase
    {
        private Poller _poller;
       
        public PollerService()
        {
            InitializeComponent();

            _poller = new Poller(this.ServiceName, this.Stop);
        }

        protected override void OnStart(string[] args)
        {
            _poller.Start();

            base.OnStart(args);
        }

        protected override void OnStop()
        {
            _poller.Stop();

            base.OnStop();
        }
    }
}
