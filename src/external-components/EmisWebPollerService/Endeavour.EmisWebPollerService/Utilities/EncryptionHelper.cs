using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Endeavour.EmisWebPollerService
{
    public static class EncryptionHelper
    {
        private static byte[] _salt = Encoding.ASCII.GetBytes("o6806642kbM7c5");
        private static string _secret = "AFED3A4E-83CE-4FB4-A560-4835BC91B6BE";        

        public static string EncryptStringAES(string plainText, string secret)
        {
            string sharedSecret = secret + _secret;

            if (string.IsNullOrEmpty(plainText))
                throw new ArgumentNullException("plainText");
            
            if (string.IsNullOrEmpty(sharedSecret))
                throw new ArgumentNullException("sharedSecret");

            string result = null;
            RijndaelManaged aesAlg = null;

            try
            {
                Rfc2898DeriveBytes key = new Rfc2898DeriveBytes(sharedSecret, _salt);

                aesAlg = new RijndaelManaged();
                aesAlg.Key = key.GetBytes(aesAlg.KeySize / 8);

                ICryptoTransform encryptor = aesAlg.CreateEncryptor(aesAlg.Key, aesAlg.IV);

                using (MemoryStream msEncrypt = new MemoryStream())
                {
                    msEncrypt.Write(BitConverter.GetBytes(aesAlg.IV.Length), 0, sizeof(int));
                    msEncrypt.Write(aesAlg.IV, 0, aesAlg.IV.Length);

                    using (CryptoStream csEncrypt = new CryptoStream(msEncrypt, encryptor, CryptoStreamMode.Write))
                        using (StreamWriter swEncrypt = new StreamWriter(csEncrypt))
                            swEncrypt.Write(plainText);

                    result = Convert.ToBase64String(msEncrypt.ToArray());
                }
            }
            finally
            {
                if (aesAlg != null)
                    aesAlg.Clear();
            }

            return result;
        }

        public static string DecryptStringAES(string cipherText, string secret)
        {
            string sharedSecret = secret + _secret;

            if (string.IsNullOrEmpty(cipherText))
                throw new ArgumentNullException("cipherText");

            if (string.IsNullOrEmpty(sharedSecret))
                throw new ArgumentNullException("sharedSecret");

            RijndaelManaged aesAlg = null;
            string result = null;

            try
            {
                Rfc2898DeriveBytes key = new Rfc2898DeriveBytes(sharedSecret, _salt);

                byte[] bytes = Convert.FromBase64String(cipherText);

                using (MemoryStream msDecrypt = new MemoryStream(bytes))
                {
                    aesAlg = new RijndaelManaged();
                    aesAlg.Key = key.GetBytes(aesAlg.KeySize / 8);
                    aesAlg.IV = ReadByteArray(msDecrypt);

                    ICryptoTransform decryptor = aesAlg.CreateDecryptor(aesAlg.Key, aesAlg.IV);

                    using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read))
                        using (StreamReader srDecrypt = new StreamReader(csDecrypt))
                            result = srDecrypt.ReadToEnd();
                }
            }
            finally
            {
                if (aesAlg != null)
                    aesAlg.Clear();
            }

            return result;
        }

        private static byte[] ReadByteArray(Stream s)
        {
            byte[] rawLength = new byte[sizeof(int)];

            if (s.Read(rawLength, 0, rawLength.Length) != rawLength.Length)
                throw new SystemException("Stream did not contain properly formatted byte array");

            byte[] buffer = new byte[BitConverter.ToInt32(rawLength, 0)];

            if (s.Read(buffer, 0, buffer.Length) != buffer.Length)
                throw new SystemException("Did not read byte array properly");

            return buffer;
        }
    }
}
