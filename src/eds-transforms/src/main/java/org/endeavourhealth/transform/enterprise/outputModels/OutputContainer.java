package org.endeavourhealth.transform.enterprise.outputModels;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.transform.common.exceptions.TransformException;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OutputContainer {

    static final String UPSERT = "Upsert";
    static final String DELETE = "Delete";

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "hh:mm:ss";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    private static final String COLUMN_CLASS_MAPPINGS = "ColumnClassMappings.json";

    private final Organization organisations;
    private final Practitioner practitioners;
    private final Schedule schedules;
    private final Person persons;
    private final Patient patients;
    private final EpisodeOfCare episodesOfCare;
    private final Appointment appointments;
    private final Encounter encounters;
    private final ReferralRequest referralRequests;
    private final ProcedureRequest procedureRequests;
    private final Observation observations;
    private final MedicationStatement medicationStatements;
    private final MedicationOrder medicationOrders;
    private final AllergyIntolerance allergyIntolerances;


    public OutputContainer(boolean pseduonymised) throws Exception {
        this(CSV_FORMAT, DATE_FORMAT, TIME_FORMAT, pseduonymised);
    }

    public OutputContainer(CSVFormat csvFormat, String dateFormat, String timeFormat, boolean pseduonymised) throws Exception {

        organisations = new Organization("organization.csv", csvFormat, dateFormat, timeFormat);
        practitioners = new Practitioner("practitioner.csv", csvFormat, dateFormat, timeFormat);
        schedules = new Schedule("schedule.csv", csvFormat, dateFormat, timeFormat);
        persons = new Person("person.csv", csvFormat, dateFormat, timeFormat, pseduonymised);
        patients = new Patient("patient.csv", csvFormat, dateFormat, timeFormat, pseduonymised);
        episodesOfCare = new EpisodeOfCare("episode_of_care.csv", csvFormat, dateFormat, timeFormat);
        appointments = new Appointment("appointment.csv", csvFormat, dateFormat, timeFormat);
        encounters = new Encounter("encounter.csv", csvFormat, dateFormat, timeFormat);
        referralRequests = new ReferralRequest("referral_request.csv", csvFormat, dateFormat, timeFormat);
        procedureRequests = new ProcedureRequest("procedure_request.csv", csvFormat, dateFormat, timeFormat);
        observations = new Observation("observation.csv", csvFormat, dateFormat, timeFormat);
        medicationStatements = new MedicationStatement("medication_statement.csv", csvFormat, dateFormat, timeFormat);
        medicationOrders = new MedicationOrder("medication_order.csv", csvFormat, dateFormat, timeFormat);
        allergyIntolerances = new AllergyIntolerance("allergy_intolerance.csv", csvFormat, dateFormat, timeFormat);
    }



    public byte[] writeToZip() throws Exception {

        //may as well zip the data, since it will compress well
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        //the first entry is a json file giving us the target class names for each column
        ObjectNode columnClassMappingJson = new ObjectNode(JsonNodeFactory.instance);

        writeColumnClassMappings(organisations, columnClassMappingJson);
        writeColumnClassMappings(practitioners, columnClassMappingJson);
        writeColumnClassMappings(schedules, columnClassMappingJson);
        writeColumnClassMappings(persons, columnClassMappingJson);
        writeColumnClassMappings(patients, columnClassMappingJson);
        writeColumnClassMappings(episodesOfCare, columnClassMappingJson);
        writeColumnClassMappings(appointments, columnClassMappingJson);
        writeColumnClassMappings(encounters, columnClassMappingJson);
        writeColumnClassMappings(referralRequests, columnClassMappingJson);
        writeColumnClassMappings(procedureRequests, columnClassMappingJson);
        writeColumnClassMappings(observations, columnClassMappingJson);
        writeColumnClassMappings(medicationStatements, columnClassMappingJson);
        writeColumnClassMappings(medicationOrders, columnClassMappingJson);
        writeColumnClassMappings(allergyIntolerances, columnClassMappingJson);

        String jsonStr = ObjectMapperPool.getInstance().writeValueAsString(columnClassMappingJson);
        zos.putNextEntry(new ZipEntry(COLUMN_CLASS_MAPPINGS));
        zos.write(jsonStr.getBytes());
        zos.flush();
        
        //then write the CSV files        
        writeZipEntry(organisations, zos);
        writeZipEntry(practitioners, zos);
        writeZipEntry(schedules, zos);
        writeZipEntry(persons, zos);
        writeZipEntry(patients, zos);
        writeZipEntry(episodesOfCare, zos);
        writeZipEntry(appointments, zos);
        writeZipEntry(encounters, zos);
        writeZipEntry(referralRequests, zos);
        writeZipEntry(procedureRequests, zos);
        writeZipEntry(observations, zos);
        writeZipEntry(medicationStatements, zos);
        writeZipEntry(medicationOrders, zos);
        writeZipEntry(allergyIntolerances, zos);

        //close
        zos.close();

        //return as base64 encoded string
        return baos.toByteArray();

    }
    
    private static void writeColumnClassMappings(AbstractEnterpriseCsvWriter csvWriter, ObjectNode columnClassMappingJson) throws Exception {

        //we only write CSV files with rows, so don't bother writing their column mappings either
        if (csvWriter.isEmpty()) {
            return;
        }

        String fileName = csvWriter.getFileName();

        //write out the column object mappings
        String[] columnNames = csvWriter.getCsvHeaders();
        Class[] classes = csvWriter.getColumnTypes();
        if (columnNames.length != classes.length) {
            throw new TransformException("Column names array (" + columnNames.length + ") isn't same length as classes array (" + classes.length + ") for " + csvWriter.getFileName());
        }

        ObjectNode jsonObject = columnClassMappingJson.putObject(fileName);

        for (int i=0; i<columnNames.length; i++) {
            String columnName = columnNames[i];
            Class cls = classes[i];
            jsonObject.put(columnName, cls.getName());
        }
    }

    private static void writeZipEntry(AbstractEnterpriseCsvWriter csvWriter, ZipOutputStream zipOutputStream) throws Exception {

        //don't bother writing empty CSV files
        if (csvWriter.isEmpty()) {
            return;
        }

        byte[] bytes = csvWriter.close();
        String fileName = csvWriter.getFileName();

        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(bytes);
        zipOutputStream.flush();
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

    public Person getPersons() {
        return persons;
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

    public MedicationOrder getMedicationOrders() {
        return medicationOrders;
    }

    public AllergyIntolerance getAllergyIntolerances() {
        return allergyIntolerances;
    }
}
