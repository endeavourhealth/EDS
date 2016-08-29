package org.endeavourhealth.sftpreader.implementations.emis;

import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.sftpreader.implementations.SftpNotificationCreator;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.db.BatchFile;
import org.endeavourhealth.sftpreader.model.db.BatchSplit;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmisSftpNotificationCreator extends SftpNotificationCreator
{
    @Override
    public String createNotificationMessage(DbConfiguration dbConfiguration, BatchSplit batchSplit) {

        Batch batch = batchSplit.getBatch();

        //sort batches by filename, so it's consistently ordered
        List<BatchFile> batchFiles = batch
                .getBatchFiles()
                .stream()
                .sorted(Comparator.comparing(t -> t.getFilename()))
                .collect(Collectors.toList());

        String relativePath = batchSplit.getLocalRelativePath();
        String rootPath = dbConfiguration.getLocalRootPath();
        String combinedPath = FilenameUtils.concat(rootPath, relativePath);
        File combinedFile = new File(combinedPath);

        List<String> files = new ArrayList<>();
        findFiles(files, combinedFile, relativePath);

        StringBuilder sb = new StringBuilder();

        for (String file: files) {
            sb.append(file);
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    private static void findFiles(List<String> files, File dir, String relativePath) {

        for (File f: dir.listFiles()) {
            String newRelativePath = FilenameUtils.concat(relativePath, f.getName());

            if (f.isDirectory()) {
                findFiles(files, f, newRelativePath);
            } else {
                files.add(newRelativePath);
            }
        }
    }
}
