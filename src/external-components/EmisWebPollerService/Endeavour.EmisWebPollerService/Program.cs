using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.Linq;
using System.Reflection;
using System.ServiceProcess;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    static class Program
    {
        static void Main(string[] args)
        {
            if (Environment.UserInteractive)
            {
                Action consoleAction = null;

                if (args.WhenNotNull(t => t.Length) == 1)
                {
                    string arg = args.First();

                    if (arg == "/i")
                        consoleAction = InstallService;
                    else if (arg == "/u")
                        consoleAction = UninstallService;
                    else if (arg == "/a")
                        consoleAction = ActivatePartnerApi;
                }

                if (consoleAction == null)
                {
                    PrintHelp();
                    return;
                }

                RunWithLogging(consoleAction);
            }
            else
            {
                ServiceBase.Run(new PollerService());
            }
        }

        private static void PrintHelp()
        {
            Console.WriteLine(string.Empty);
            Console.WriteLine("Endeavour.EmisWebPollerService.exe");
            Console.WriteLine(string.Empty);
            Console.WriteLine(" /i   Install service (requires administrative privileges)");
            Console.WriteLine(" /u   Uninstall service (requires administrative privileges)");
            Console.WriteLine(" /a   Activate partner API");
            Console.WriteLine(string.Empty);
        }

        private static void RunWithLogging(Action method)
        {
            Log.WriteLogDivider();
            Log.Write(method.Method.Name);

            try
            {
                method();
            }
            catch (Exception ex)
            {
                Log.WriteToConsoleAndLog(ex);
            }

            Log.Write("{0} end", method.Method.Name);
        }

        private static void InstallService()
        {
            Configuration.Configuration configuration = ConfigurationManager.ReadConfiguration();

            ManagedInstallerClass.InstallHelper(new string[] { Assembly.GetExecutingAssembly().Location });
        }

        private static void UninstallService()
        {
            ManagedInstallerClass.InstallHelper(new string[] { "/u", Assembly.GetExecutingAssembly().Location });
        }

        private static void ActivatePartnerApi()
        {
            Configuration.Configuration configuration = ConfigurationManager.ReadConfiguration();

            EmisPartnerInterface emisInterface = new EmisPartnerInterface();

            InitialiseWithIdResult initializeResult = emisInterface.InitializeWithId(configuration.EmisWebIPAddress, configuration.EmisWebCdb, configuration.PartnerApiKey);
            emisInterface.Logon(initializeResult.LoginId, configuration.PartnerApiUserName, configuration.PartnerApiPassword);

            Log.WriteToConsoleAndLog(string.Empty);
            Log.WriteToConsoleAndLog("Partner API activation succeeded");
        }
    }
}
