package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OutputContainer {

    public static final String UPSERT = "Upsert";
    public static final String DELETE = "Delete";

    private final Organization organisations;
    private final Practitioner practitioners;
    private final Schedule schedules;

    private final Patient patients;


    private final AllergyIntolerance allergyIntolerances;

    public OutputContainer(CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        organisations = new Organization("organization.csv", csvFormat, dateFormat, timeFormat);
        practitioners = new Practitioner("practitioner.csv", csvFormat, dateFormat, timeFormat);
        schedules = new Schedule("schedule.csv", csvFormat, dateFormat, timeFormat);

        patients = new Patient("patient.csv", csvFormat, dateFormat, timeFormat);


        allergyIntolerances = new AllergyIntolerance("allergy_intolerance.csv", csvFormat, dateFormat, timeFormat);
    }



    public byte[] writeToZip() throws Exception {

        //may as well zip the data, since it will compress well
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        writeZipEntry(organisations, zos);
        writeZipEntry(practitioners, zos);
        writeZipEntry(schedules, zos);

        writeZipEntry(patients, zos);


        writeZipEntry(allergyIntolerances, zos);


        zos.close();

        //return as base64 encoded string
        return baos.toByteArray();

    }

    private static void writeZipEntry(AbstractCsvWriter csvWriter, ZipOutputStream zipOutputStream) throws Exception {

        byte[] bytes = csvWriter.close();
        String fileName = csvWriter.getFileName();

        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(bytes);
        zipOutputStream.flush();
    }
}
