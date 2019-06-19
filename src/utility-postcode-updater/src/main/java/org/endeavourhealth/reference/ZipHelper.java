package org.endeavourhealth.reference;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ZipHelper.class);

    public static boolean isZip(File f) {
        String ext = FilenameUtils.getExtension(f.getName());
        return ext.equalsIgnoreCase("zip");
    }

    public static ZipInputStream createZipInputStream(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        return new ZipInputStream(bis);
    }

    public static Reader findFile(ZipInputStream zis, String fileNameRegex) throws Exception {

        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                break;
            }

            String entryFileName = entry.getName();
            //LOG.debug("Found " + entryFileName);
            if (Pattern.matches(fileNameRegex, entryFileName)) {
                LOG.debug("Matched on file " + entryFileName);
                BufferedInputStream bis = new BufferedInputStream(zis);
                return new InputStreamReader(bis);
            }
        }

        return null;
    }

}
