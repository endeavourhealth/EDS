using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    internal static class ExtensionMethods
    {
        public static T GetAs<T>(this object[] parameters, int index)
        {
            object obj = parameters[index];

            if (obj == null)
                return default(T);

            return (T)obj;
        }

        public static R WhenNotNull<T, R>(this T source, Func<T, R> selector)
        {
            if (source == null)
                return default(R);

            return selector(source);
        }

        public static string GetValue(this Dictionary<string, string> dictionary, string key)
        {
            if (!dictionary.ContainsKey(key))
                throw new KeyNotFoundException(string.Format("Could not find configuration value '{0}'", key));

            return dictionary[key];
        }
    }
}
