package org.endeavourhealthX.subscriber;

import java.io.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CegUnzipper {

    public static void unzip(String base64, File directory) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        try {
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                byte[] buffer = new byte[2048];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos, buffer.length);

                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();

                String csvFileName = entry.getName();
                byte[] csvFileBytes = baos.toByteArray();
                writeFile(csvFileName, directory, csvFileBytes);
            }
        } finally {
            zis.close();
        }
    }

    private static void writeFile(String fileName, File dir, byte[] bytes) throws Exception {
        File file = new File(dir, fileName);
        if (file.exists()) {
            writeExistingFile(file, bytes);
        } else {
            writeNewFile(file, bytes);
        }
    }

    private static void writeNewFile(File file, byte[] bytes) throws Exception {

        //if writing a new file, just write the bytes out
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file)); ) {

            bos.write(bytes);
            bos.flush();
        }
    }

    private static void writeExistingFile(File file, byte[] bytes) throws Exception {

        //if the file already exists, we need to append our content, but WITHOUT the CSV header row
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true))) ) {

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {

                //read and discard the headers
                br.readLine();

                String line = null;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }

                pw.flush();
            }
        }
    }
}
