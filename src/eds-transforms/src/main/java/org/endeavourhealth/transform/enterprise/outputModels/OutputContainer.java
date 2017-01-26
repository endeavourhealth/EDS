package org.endeavourhealth.transform.enterprise.outputModels;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.transform.common.exceptions.TransformException;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OutputContainer {

    public static final String UPSERT = "Upsert";
    public static final String DELETE = "Delete";

    public static final String COLUMN_CLASS_MAPPINGS = "ColumnClassMappings.json";

    private final Organization organisations;
    private final Practitioner practitioners;
    private final Schedule schedules;
    private final Patient patients;
    private final EpisodeOfCare episodesOfCare;
    private final Appointment appointments;
    private final Encounter encounters;
    private final ReferralRequest referralRequests;
    private final ProcedureRequest procedureRequests;
    private final Observation observations;
    private final MedicationStatement medicationStatements;
    private final MedicationOrder medicationOrder;
    private final AllergyIntolerance allergyIntolerances;


    public OutputContainer(CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        organisations = new Organization("organization.csv", csvFormat, dateFormat, timeFormat);
        practitioners = new Practitioner("practitioner.csv", csvFormat, dateFormat, timeFormat);
        schedules = new Schedule("schedule.csv", csvFormat, dateFormat, timeFormat);
        patients = new Patient("patient.csv", csvFormat, dateFormat, timeFormat);
        episodesOfCare = new EpisodeOfCare("episode_of_care.csv", csvFormat, dateFormat, timeFormat);
        appointments = new Appointment("appointment.csv", csvFormat, dateFormat, timeFormat);
        encounters = new Encounter("encounter.csv", csvFormat, dateFormat, timeFormat);
        referralRequests = new ReferralRequest("referral_request.csv", csvFormat, dateFormat, timeFormat);
        procedureRequests = new ProcedureRequest("procedure_request.csv", csvFormat, dateFormat, timeFormat);
        observations = new Observation("observation.csv", csvFormat, dateFormat, timeFormat);
        medicationStatements = new MedicationStatement("medication_statement.csv", csvFormat, dateFormat, timeFormat);
        medicationOrder = new MedicationOrder("medication_order.csv", csvFormat, dateFormat, timeFormat);
        allergyIntolerances = new AllergyIntolerance("allergy_intolerance.csv", csvFormat, dateFormat, timeFormat);
    }



    public byte[] writeToZip() throws Exception {

        //may as well zip the data, since it will compress well
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        ObjectNode columnClassMappingJson = new ObjectNode(JsonNodeFactory.instance);

        writeZipEntry(organisations, zos, columnClassMappingJson);
        writeZipEntry(practitioners, zos, columnClassMappingJson);
        writeZipEntry(schedules, zos, columnClassMappingJson);
        writeZipEntry(patients, zos, columnClassMappingJson);
        writeZipEntry(episodesOfCare, zos, columnClassMappingJson);
        writeZipEntry(appointments, zos, columnClassMappingJson);
        writeZipEntry(encounters, zos, columnClassMappingJson);
        writeZipEntry(referralRequests, zos, columnClassMappingJson);
        writeZipEntry(procedureRequests, zos, columnClassMappingJson);
        writeZipEntry(observations, zos, columnClassMappingJson);
        writeZipEntry(medicationStatements, zos, columnClassMappingJson);
        writeZipEntry(medicationOrder, zos, columnClassMappingJson);
        writeZipEntry(allergyIntolerances, zos, columnClassMappingJson);

        //write the mappings entry
        String jsonStr = ObjectMapperPool.getInstance().writeValueAsString(columnClassMappingJson);
        zos.putNextEntry(new ZipEntry(COLUMN_CLASS_MAPPINGS));
        zos.write(jsonStr.getBytes());
        zos.flush();

        //close
        zos.close();

        //return as base64 encoded string
        return baos.toByteArray();

    }

    private static void writeZipEntry(AbstractEnterpriseCsvWriter csvWriter, ZipOutputStream zipOutputStream, ObjectNode columnClassMappingJson) throws Exception {

        byte[] bytes = csvWriter.close();
        String fileName = csvWriter.getFileName();

        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(bytes);
        zipOutputStream.flush();

        //write out the column object mappings
        String[] columnNames = csvWriter.getCsvHeaders();
        Class[] classes = csvWriter.getColumnTypes();
        if (columnNames.length != classes.length) {
            throw new TransformException("Column names array (" + columnNames.length + ") isn't same length as classes array (" + classes.length + ")");
        }

        ObjectNode jsonObject = columnClassMappingJson.putObject(fileName);

        for (int i=0; i<columnNames.length; i++) {
            String columnName = columnNames[i];
            Class cls = classes[i];
            jsonObject.put(columnName, cls.getName());
        }
    }

    public Organization getOrganisations() {
        return organisations;
    }

    public Practitioner getPractitioners() {
        return practitioners;
    }

    public Schedule getSchedules() {
        return schedules;
    }

    public Patient getPatients() {
        return patients;
    }

    public EpisodeOfCare getEpisodesOfCare() {
        return episodesOfCare;
    }

    public Appointment getAppointments() {
        return appointments;
    }

    public Encounter getEncounters() {
        return encounters;
    }

    public ReferralRequest getReferralRequests() {
        return referralRequests;
    }

    public ProcedureRequest getProcedureRequests() {
        return procedureRequests;
    }

    public Observation getObservations() {
        return observations;
    }

    public MedicationStatement getMedicationStatements() {
        return medicationStatements;
    }

    public MedicationOrder getMedicationOrder() {
        return medicationOrder;
    }

    public AllergyIntolerance getAllergyIntolerances() {
        return allergyIntolerances;
    }
}
