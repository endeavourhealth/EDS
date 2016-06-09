using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal static class Log
    {
        private static string ServiceName;
        
        public static void RegisterServiceName(string serviceName)
        {
            ServiceName = serviceName;
        }
        
        public static void Write(string text, bool writeToEventLogOnFailure, params string[] formatArgs)
        {
            try
            {
                if (formatArgs != null)
                    if (formatArgs.Length > 0)
                        text = string.Format(text, formatArgs);
                
                string logFilePath = Path.Combine(FilePaths.LogPath, GetLogFileName());

                Utilities.AppendToTextFile(logFilePath, DateTime.Now.ToString() + " " + text + Environment.NewLine);
            }
            catch (Exception e)
            {
                try
                {
                    if (writeToEventLogOnFailure)
                    {
                        string message = "Exception occurred writing to log file." + Environment.NewLine + Environment.NewLine + e.ToFormattedString();

                        WriteToEventLog(message);
                    }
                }
                catch
                {
                    // do nothing
                }
            }
        }
        
        public static void WriteLogDivider()
        {
            Write("-------------------------------------------------------------------------------------------");
        }

        public static void Write(string message, Exception e)
        {
            Write(e.ToFormattedString());
            Write(message);
        }

        public static void Write(string text, bool writeToEventLogOnFailure)
        {
            Write(text, false, null);
        }

        public static void Write(string text, params string[] formatArgs)
        {
            Write(text, false, formatArgs);
        }

        public static void WriteToConsoleAndLog(string message)
        {
            Log.Write(message);
            Console.WriteLine(message);
        }

        public static void WriteToConsoleAndLog(Exception exception)
        {
            WriteToConsoleAndLog(string.Empty);
            WriteToConsoleAndLog("Error: " + exception.Message);

            if (exception.InnerException != null)
                WriteToConsoleAndLog(exception.InnerException.Message);
        }

        private static void WriteToEventLog(string message)
        {
            EventLog.WriteEntry(ServiceName, message, EventLogEntryType.FailureAudit, -99);
        }

        private static string GetLogFileName()
        {
            return DateTime.Now.Date.ToString("yyyy-MMM-dd") + ".Endeavour.EmisWebPollerService.log";
        }
    }
}
