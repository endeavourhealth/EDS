package org.endeavourhealth.messaging.utilities;

import org.apache.commons.io.IOUtils;
import org.endeavourhealth.messaging.exceptions.MsgxException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileHelper
{
    public static String loadStringResource(String resourceLocation) throws Exception
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceLocation);

        if (url == null)
            throw new MsgxException(resourceLocation + " not found");

        InputStream stream = url.openStream();
        String resource = IOUtils.toString(stream);
        IOUtils.closeQuietly(stream);

        return resource;
    }

    public static String loadStringFile(String location) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(location));
        return new String(encoded, "UTF-8");
    }

    public static String combinePaths(String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    public static boolean createDirectory(String directory)
    {
        File file = new File(directory);

        if (!file.exists())
            return file.mkdir();

        return true;
    }

    public static String findFileRecursive(final File directoryStart, final String filename)
    {
        Stack<File> files = new Stack<>();
        files.addAll(Arrays.asList(directoryStart.listFiles()));

        while (!files.isEmpty())
        {
            File file = files.pop();

            if (file.getName().toLowerCase().equals(filename.toLowerCase()))
                return file.getPath();

            if (file.isDirectory())
                files.addAll(Arrays.asList(file.listFiles()));
        }

        return null;
    }

    public static String findFileInJar(File jarFile, String filename) throws IOException
    {
        ZipFile zipFile = new ZipFile(jarFile);

        try
        {
            Enumeration zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements())
            {
                ZipEntry zipEntry = (ZipEntry)zipEntries.nextElement();

                if (new File(zipEntry.getName()).getName().equals(filename))
                    return zipEntry.getName();
            }
        }
        finally
        {
            zipFile.close();
        }

        return null;
    }
}
