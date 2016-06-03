package org.endeavourhealth.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static String readTextFile(Path file) throws Exception {

        try {
            if (!Files.exists(file))
                throw new Exception("Could not find file: " + file.getFileName());

            byte[] encoded = Files.readAllBytes(file);
            return new String(encoded, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new Exception("Error reading file: " + file.getFileName(), e);
        }
    }

    public static boolean pathNotExists(String path) {
        Path pathObject = Paths.get(path);
        return Files.notExists(pathObject);
    }

    public static void createFolder(Path folder) throws IOException {
        Files.createDirectory(folder);
    }
}
