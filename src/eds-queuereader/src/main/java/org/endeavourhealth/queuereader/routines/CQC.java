package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import jdk.nashorn.internal.objects.NativeJSON;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientLinkPair;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.PseudoIdDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberPersonMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.im.client.IMClient;
import org.endeavourhealth.im.models.mapping.MapColumnRequest;
import org.endeavourhealth.im.models.mapping.MapColumnValueRequest;
import org.endeavourhealth.im.models.mapping.MapResponse;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.common.resourceBuilders.*;
import org.endeavourhealth.transform.enterprise.EnterpriseTransformHelper;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.enterprise.transforms.EpisodeOfCareEnterpriseTransformer;
import org.endeavourhealth.transform.enterprise.transforms.PatientEnterpriseTransformer;
import org.endeavourhealth.transform.hl7v2fhir.transforms.OrganizationTransformer;
import org.endeavourhealth.transform.subscriber.*;
import org.endeavourhealth.transform.subscriber.targetTables.OrganizationContact_v2;
import org.endeavourhealth.transform.subscriber.targetTables.OutputContainer;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.endeavourhealth.transform.subscriber.transforms.EpisodeOfCareTransformer;
import org.endeavourhealth.transform.subscriber.transforms.OrganisationTransformer;
import org.endeavourhealth.transform.subscriber.transforms.OrganisationTransformer_v2;
import org.endeavourhealth.transform.subscriber.transforms.PatientTransformer;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.utilities.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.System;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import java.sql.*;
import javax.persistence.EntityManager;
import org.hibernate.internal.SessionImpl;
import org.hl7.fhir.instance.model.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public abstract class CQC extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SpecialRoutines.class);

    // test stub that's run on laptops
    public static void CQC() {

        //ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        //ResourceWrapper wrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Organization.toString(), orgId);
        //ResourceWrapper var13;

        try {

            //LoadODSFile("D:\\TEMP\\ODSFILES\\ecarehomesite.csv");
            //findODSCode("D:\\TEMP\\ODSFILES\\","BH23 5RT");

            /*
            List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections("subscriber_test");
            EnterpriseConnector.ConnectionWrapper connectionWrapper = connectionWrappers.get(0);
            Connection subscriberConnection = connectionWrapper.getConnection();

            String sql ="insert into ods_v2(code) values('test');";
            PreparedStatement preparedStmt = null;
            preparedStmt = subscriberConnection.prepareStatement(sql);
            //preparedStmt.setString(1,cqc_id);
            //preparedStmt.setString(2,resource_guid);
            boolean z = preparedStmt.execute();
            System.out.println(z);
            preparedStmt.close();

            subscriberConnection.close();
             */

            //LoadODSData("D:\\TEMP\\ODSFILES\\");

            //OrganizationBuilder();

            //SplitBuilderFile("C:\\Users\\PaulSimon\\Desktop\\builder.txt","d:\\temp\\builder\\");

            //UUID serviceId = UUID.fromString("9d23eb25-b710-4c8b-a0ef-793b3df68c29");
            UUID serviceId = UUID.fromString("2bb86ffb-c36f-4f8d-a419-60f6bcb7c0dc"); // where does this get logged (it does'nt)

            /* so that we can query patient_address_match, or whatever else
            List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections("subscriber_test");
            EnterpriseConnector.ConnectionWrapper connectionWrapper = connectionWrappers.get(0);
            Connection subscriberConnection = connectionWrapper.getConnection();
            String sql = "select * from organization";

            PreparedStatement ps = subscriberConnection.prepareStatement(sql);
            ResultSet  rs = ps.executeQuery();

                //EntityManager edsEntityManager = ConnectionManager.getEdsEntityManager();
                //SessionImpl session = (SessionImpl) edsEntityManager.getDelegate();
                //Connection edsConnection = session.connection();

                //Statement s = edsConnection.createStatement();
                //ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                System.out.println(">> " + rs.getString("name"));
            }
            subscriberConnection.close();
             */

            // from the bottom:
            // abp_classification
            // abp_address
            // property
            // address
            // location_additional
            // organization_additional
            // organization

            //String pathToCsv = "C:\\Users\\PaulSimon\\Desktop\\org-json.txt";
            //String pathToCsv = "C:\\Users\\PaulSimon\\Desktop\\org-json - Copy.txt";
            //String pathToCsv = "C:\\Users\\PaulSimon\\Desktop\\org-json-contained.txt";

            //String pathToCsv = "C:\\Users\\PaulSimon\\Desktop\\builder0.txt";
            //String pathToCsv = "d:\\temp\\builder\\builder0.txt";

            String pathToCsv = "C:\\Users\\PaulSimon\\Desktop\\builder_single.txt";

            BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row = ""; String resource_guid = ""; String json = "";
            while ((row = csvReader.readLine()) != null) {
                String[] ss = row.split("\t", -1);
                resource_guid = ss[0];
                json = ss[1];
                System.out.println(resource_guid + " * " + json);

                ResourceWrapper w = new ResourceWrapper();

                // if the organization contains a Location, then that needs to be added first
                // unless the system does not check for the existence of the location?

                //UUID serviceId = UUID.fromString("9d23eb25-b710-4c8b-a0ef-793b3df68c29");
                w.setServiceId(serviceId);

                // this is the one that worked
                //w.setSystemId(UUID.fromString("55c08fa5-ef1e-4e94-aadc-e3d6adc80774"));

                w.setSystemId(UUID.fromString("f3ded58f-dec5-4a21-bd43-9ec307cd1b60")); // random uuids work ok as well?

                w.setResourceType("Organization");
                UUID zuid = UUID.randomUUID();
                //w.setResourceId(zuid);
                //w.setResourceId(UUID.fromString("7bbdc091-1b20-47b1-ae82-3aae42b8fd58"));

                w.setResourceId(UUID.fromString(resource_guid));

                //w.setResourceData("{\"resourceType\":\"Organization\",\"id\":\"b5de32be-eb55-46ab-bc3a-a277d160e0da\",\"meta\":{\"profile\":[\"http://endeavourhealth.org/fhir/StructureDefinition/primarycare-organization\"]},\"name\":\"PS Test Organization\",\"address\":[{\"use\":\"work\",\"text\":\"King Lane, Leeds, West Yorkshire, LS175EH\",\"line\":[\"King Lane\"],\"city\":\"Leeds\",\"district\":\"West Yorkshire\",\"postalCode\":\"LS175EH\"}]}");
                //w.setResourceData("{\"resourceType\":\"Organization\",\"id\":\"b5de32be-eb55-46ab-bc3a-a277d160e0da\",\"meta\":{\"profile\":[\"http://endeavourhealth.org/fhir/StructureDefinition/primarycare-organization-v2\"]},\"extension\":[{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/primarycare-mainlocation-extension\",\"valueReference\":{\"reference\":\"Location/e0760369-e221-410f-bbaf-385f3567a004\"}},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-id-extension\",\"valueString\":\"6363\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-services-web-site-extension\",\"valueString\":\"http://www.mcch.co.uk\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-service-type-extension\",\"valueString\":\"Residential homes\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-date-of-latest-check-extension\",\"valueString\":\"07/02/2020 - 00:00\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-specialisms-services-extension\",\"valueString\":\"Learning disabilities|Accommodation for persons who require nursing or personal care\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-provider-name-extension\",\"valueString\":\"Choice Support\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-region-extension\",\"valueString\":\"London\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-location-url-extension\",\"valueString\":\"https://www.cqc.org.uk/location/1-124233374\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-location-extension\",\"valueString\":\"1-124233374\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-provider-id-extension\",\"valueString\":\"1-117003409\"},{\"url\":\"http://endeavourhealth.org/fhir/StructureDefinition/cqc-on-ratings-extension\",\"valueString\":\"Y\"}],\"name\":\"Perrymans\",\"telecom\":[{\"system\":\"phone\",\"value\":\"2085967850\"}],\"address\":[{\"use\":\"work\",\"text\":\"56a Abbey Road, Barkingside, Ilford, IG2 7NA\",\"line\":[\"56a Abbey Road\",\"Barkingside\"],\"district\":\"Ilford\",\"postalCode\":\"IG2 7NA\"}],\"partOf\":{\"reference\":\"Organization/6d0b1f75-168a-42c0-82ba-7e434fc10942\"}}");
                //w.setResourceData("{\"resourceType\":\"Organization\",\"id\": \"091555c7-76d3-4cb2-919f-4a5a8f6b8463\",\"meta\": {\"profile\": [\"http://endeavourhealth.org/fhir/StructureDefinition/primarycare-organization-v2\"]},\"name\": \"Paediatric and Special Care Community Dental Service St Leonards Hospital\",\"telecom\": [{\"system\": \"phone\",\"value\": \"3007900158\"}],\"address\": [{\"use:\": \"work\",\"text\": \"St Leonards Hospital, Nuttall Street, London,N1 5LZ\",\"line\": [\"39 NUTTALL STREET\",\"SHOREDITCH\",\"\"],\"district\": \"GREATER LONDON\",\"postalCode\": \"N1 5LZ\"}]}");
                //w.setResourceData("{\"resourceType\":\"Organization\",\"id\":\"7bbdc091-1b20-47b1-ae82-3aae42b8fd58\",\"meta\":{\"profile\":[\"http://endeavourhealth.org/fhir/StructureDefinition/primarycare-organization-v2\"]},\"identifier\":[{\"system\":\"http://fhir.nhs.net/Id/ods-organization-code\",\"value\":\"VL15A\"}],\"name\":\"Mawney Road\",\"telecom\":[{\"system\":\"phone\",\"value\":\"1708741388\"}],\"address\":[{\"use:\":\"work\",\"text\":\"89 Mawney Road, Romford,RM7 7HX\",\"line\":[\"89 MAWNEY ROAD\",\"\",\"\"],\"district\":\"ESSEX\",\"postalCode\":\"RM7 7HX\"}]}");

                w.setResourceData(json);
                w.setCreatedAt(new java.util.Date(System.currentTimeMillis()));

                List<ResourceWrapper> resourceWrappers = new ArrayList<>();
                resourceWrappers.add(w);

                String subscriberConfigName = "subscriber_test";
                SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
                SubscriberTransformHelper helper = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, resourceWrappers, false);

                //Long enterpriseOrgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, helper, Collections.emptyList());
                //helper.setSubscriberOrganisationId(enterpriseOrgId);

                //OrganisationTransformer t = new OrganisationTransformer();

                OrganisationTransformer_v2 t = new OrganisationTransformer_v2();
                t.transformResources(resourceWrappers, helper);

                OutputContainer output = helper.getOutputContainer();
                byte[] bytes = output.writeToZip();

                String base64 = Base64.getEncoder().encodeToString(bytes);
                UUID batchId = UUID.randomUUID();
                UUID queuedMessageId = UUID.randomUUID();

                SubscriberFiler.file(batchId, queuedMessageId, base64, subscriberConfigName);
            }

            csvReader.close();

            } catch (Throwable t) {
                LOG.error("", t);
            }
    }

    public static void CQCOrganizationBuilder(String configName, String filename, String builderFilename) throws Exception
    {
        // subscriber_test, D:\\TEMP\\COVID-Jiras\\08_July_2020_CQC_directory_NEL8.csv, C:\\Users\\PaulSimon\\Desktop\\builder.txt
        OrganizationBuilder(configName, filename, builderFilename);
    }

    public static void ThreadCQC(String configName, String serviceUUID, String builderFilename, String systemId) throws Exception
    {
        try {

            UUID serviceId = UUID.fromString(serviceUUID);
            ThreadCQCFile(builderFilename,serviceId,configName, systemId);

            //UUID serviceId = UUID.fromString("2bb86ffb-c36f-4f8d-a419-60f6bcb7c0dc"); // where does this get logged (it does'nt)
            //ThreadCQCFile("C:\\Users\\PaulSimon\\Desktop\\builder.txt",serviceId,"subscriber_test");
            //ThreadCQCFile("C:\\Users\\PaulSimon\\Desktop\\builder_single.txt",serviceId,"subscriber_test");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    static class CQCCallable implements Callable {
        private String row;
        private UUID serviceUUID;
        private String subscriberConfigName;
        private String systemId;

        public CQCCallable(String row, UUID serviceUUID, String subscriberConfigName, String systemId) {
            this.row = row;
            this.serviceUUID = serviceUUID;
            this.subscriberConfigName = subscriberConfigName;
            this.systemId = systemId;
        }

        @Override
        public Object call() throws Exception {
            try {
            String[] ss = row.split("\t", -1);
            String resource_guid = ss[0]; String json = ss[1];

            ResourceWrapper w = new ResourceWrapper();
            w.setServiceId(serviceUUID);

            // random uuid => w.setSystemId(UUID.fromString("f3ded58f-dec5-4a21-bd43-9ec307cd1b60"));
            w.setSystemId(UUID.fromString(systemId));

            w.setResourceType("Organization");
            w.setResourceId(UUID.fromString(resource_guid));

            w.setResourceData(json);
            w.setCreatedAt(new java.util.Date(System.currentTimeMillis()));

            List<ResourceWrapper> resourceWrappers = new ArrayList<>();
            resourceWrappers.add(w);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
            SubscriberTransformHelper helper = new SubscriberTransformHelper(serviceUUID, null, null, null, subscriberConfig, resourceWrappers, false);

            OrganisationTransformer_v2 t = new OrganisationTransformer_v2();
            t.transformResources(resourceWrappers, helper);

            OutputContainer output = helper.getOutputContainer();
            byte[] bytes = output.writeToZip();

            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            UUID queuedMessageId = UUID.randomUUID();

            SubscriberFiler.file(batchId, queuedMessageId, base64, subscriberConfigName);

            } catch (Throwable t) {
                LOG.error("", t);
            }

            return null;
        }
    }

    public static void ThreadCQCFile(String builderFile, UUID serviceUUID, String subscriberConfigName, String systemId) throws Exception
    {
        BufferedReader csvReader = new BufferedReader(new FileReader(builderFile));
        String row = ""; String resource_guid = ""; String json = "";
        Integer threads = 3;
        Integer QBeforeBlock = 4;
        ThreadPool threadPool = new ThreadPool(threads, QBeforeBlock);
        while ((row = csvReader.readLine()) != null) {
            List<ThreadPoolError>  errors = threadPool.submit(new CQCCallable(row, serviceUUID, subscriberConfigName, systemId));
            handleErrors(errors);
        }
        List<ThreadPoolError> errors = threadPool.waitAndStop();
        handleErrors(errors);
    }

    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        Throwable cause = first.getException();
        //the cause may be an Exception or Error so we need to explicitly
        //cast to the right type to throw it without changing the method signature
        if (cause instanceof Exception) {
            throw (Exception)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        }
    }

    /*
    public static void SplitBuilderFile(String pathtoTxt, String outputFolder) throws Exception
    {
        Integer count = 0;
        BufferedReader csvReader = new BufferedReader(new FileReader(pathtoTxt));
        FileWriter csvWriter = new FileWriter(outputFolder+"builder"+count.toString()+".txt");
        String row = "";
        while ((row = csvReader.readLine()) != null) {
            csvWriter.write(row+"\n");
            count++;
            if (count%100 == 0) {
                System.out.println("create a new file"+count);
                csvWriter.close();
                String file = outputFolder+"builder"+count.toString()+".txt";
                csvWriter = new FileWriter(outputFolder+"builder"+count.toString()+".txt");
            }
        }
        csvReader.close();
        csvWriter.close();
    }
     */

    public static void LoadODSFile2(String filedir, String filename) throws Exception
    {
        String configName = "subscriber_test";
        List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections(configName);
        EnterpriseConnector.ConnectionWrapper connectionWrapper = connectionWrappers.get(0);

        Connection subscriberConnection = connectionWrapper.getConnection();

        Reader reader = Files.newBufferedReader(Paths.get(filedir));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        String ret = "";

        String d = "','";

        for (CSVRecord csvRecord : csvParser) {
            String postcode = csvRecord.get(9);

            String ods_code = csvRecord.get(0);
            String name = csvRecord.get(1);
            String addline1 = csvRecord.get(4);
            String addline2 = csvRecord.get(5);
            String addline3 = csvRecord.get(6);
            String addline4 = csvRecord.get(7);
            String addline5 = csvRecord.get(8);
            String parent_organization = csvRecord.get(14);
            String parent_current = csvRecord.get(23);
            String opendate = csvRecord.get(10);
            String closedate = csvRecord.get(11);

            String adrec = addline1+","+addline2+","+addline3+","+addline4+","+addline5+","+postcode;
            String uprn = getUPRN(adrec, "odsload`"+ods_code+"`"+filename);

            if (uprn.isEmpty()) {
                // try including the name in the address string
                adrec = name+","+addline1+","+addline2+","+addline3+","+addline4+","+addline5+","+postcode;
                uprn = getUPRN(adrec, "odsload`"+ods_code+"`"+filename);
            }

            String sql = "INSERT INTO ods_v2 (ods_code,name,postcode,filename,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,uprn,parent_organization,parent_current,opendate,closedate) ";
            sql = sql + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            System.out.println(sql);

            PreparedStatement preparedStmt = null;
            preparedStmt = subscriberConnection.prepareStatement(sql);

            preparedStmt.setDate(13,null);
            if (!opendate.isEmpty()) {
                opendate = opendate.substring(0,4)+"-"+opendate.substring(4,6)+"-"+opendate.substring(6,8);
                preparedStmt.setDate(13,java.sql.Date.valueOf(opendate));
            }

            preparedStmt.setDate(14,null);
            if (!closedate.isEmpty()) {
                closedate = closedate.substring(0,4)+"-"+closedate.substring(4,6)+"-"+closedate.substring(6,8);
                preparedStmt.setDate(14,java.sql.Date.valueOf(closedate));
            }

            preparedStmt.setString(1,ods_code);
            preparedStmt.setString(2,name);
            preparedStmt.setString(3,postcode.replaceAll(" ","").toLowerCase());
            preparedStmt.setString(4,filename);
            preparedStmt.setString(5,addline1);
            preparedStmt.setString(6,addline2);
            preparedStmt.setString(7,addline3);
            preparedStmt.setString(8,addline4);
            preparedStmt.setString(9,addline5);
            preparedStmt.setString(10,uprn);
            preparedStmt.setString(11,parent_organization);
            preparedStmt.setString(12,parent_current);

            preparedStmt.executeUpdate();
            subscriberConnection.commit();
            preparedStmt.close();
        }

        subscriberConnection.close();
    }

    public static String LoadODSFile(String filename, String postcode) throws Exception
    {
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        String ret = "";
        for (CSVRecord csvRecord : csvParser) {
            String csv_postcode = csvRecord.get(9);
            if (csv_postcode.equals(postcode)) {
                String code = csvRecord.get(0);
                String addline1 = csvRecord.get(4);
                String addline2 = csvRecord.get(5);
                String addline3 = csvRecord.get(6);
                String addline4 = csvRecord.get(7);
                String addline5 = csvRecord.get(8);
                ret = ret+code+"~"+addline1+"~"+addline2+"~"+addline3+"~"+addline4+"~"+addline5+"~"+csv_postcode;
                break;
            }
        }
        return ret;
    }

    public static void LoadODSData(String DirectoryPath) throws Exception
    {
        File dir = new File(DirectoryPath);
        File[] directoryListing = dir.listFiles();
        for (File child : directoryListing) {
            String filename = child.toString();
            String[] ss = filename.split("\\.");
            String ext = ss[ss.length-1];
            if (!ext.contains("csv")) {continue;}
            String file = ss[ss.length-2];
            String[] f = file.split("\\\\");
            System.out.println(f[3]);
            LoadODSFile2(filename, f[3]);
        }
    }

    public static String FindODSCodeUsingUprn(String postcode, String sFilename, String uprn, String name, String configName) throws Exception
    {
        // String configName = "subscriber_test";

        //List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections(configName);
        //EnterpriseConnector.ConnectionWrapper connectionWrapper = connectionWrappers.get(0);

        // switch to subscriber_transform database
        Connection connection = ConnectionManager.getSubscriberTransformConnection(configName);

        //Connection subscriberConnection = connectionWrapper.getConnection();

        postcode = postcode.replaceAll(" ","").toLowerCase();

        //String sql = "select * from ods_v2 where uprn = '"+uprn+"' and filename='"+sFilename+"'";

        // get the first word in name and do a sql like
        name = name.replaceAll("'","\\\\'");

        String[] ss = name.split(" ",-1);
        String first_word = ss[0];

        //first_word = first_word.replaceAll("'","\\\\'");

        // count the number of records in result set for uprn?
        // or, just check organization name is the same?
        // String sql = "select * from ods_v2 where uprn = '"+uprn+"' and lower(name) like '%"+first_word.toLowerCase()+"%'";

        String sql = "select * from ods_v2 where uprn = '"+uprn+"' and lower(name) ='"+name.toLowerCase()+"'";

        //PreparedStatement ps = subscriberConnection.prepareStatement(sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet  rs = ps.executeQuery();

        String ret = "";
        if (rs.next()) {
            String id = rs.getString("id");
            String ods_code = rs.getString("ods_code");
            String filename = rs.getString("filename");
            String address_line_1 = rs.getString("address_line_1");
            String address_line_2 = rs.getString("address_line_2");
            String address_line_3 = rs.getString("address_line_3");
            String address_line_4 = rs.getString("address_line_4");
            String address_line_5 = rs.getString("address_line_5");
            String parent_organization = rs.getString("parent_organization");
            String parent_current = rs.getString("parent_current");
            String opendate = rs.getString("opendate");
            String closedate = rs.getString("closedate");

            if (parent_organization==null) parent_organization="";
            if (parent_current==null) parent_current="";
            if (opendate==null) opendate="";
            if (closedate==null) closedate="";

            ret = id+"~"+ods_code+"~"+address_line_1+"~"+address_line_2+"~"+address_line_3+"~"+address_line_4+"~"+address_line_5+"~"+filename+"~";
            ret = ret+"~"+parent_organization+"~"+parent_current+"~"+opendate+"~"+closedate;
        }
        ps.close();

        //subscriberConnection.close();

        connection.close();
        return ret;
    }

    public static boolean indexInBound(String[] data, int index){
        return data != null && index >= 0 && index < data.length;
    }

    private static String getUPRN(String adrec, String id) throws Exception
    {
        String csv = UPRN.getAdrec(adrec, id);
        if (Strings.isNullOrEmpty(csv)) {System.out.println("Unable to get address from UPRN API");}

        String uprn = "";
        if (!csv.isEmpty()) {
            String ss[] = csv.split("\\~",-1);
            uprn = ss[20];
        }
        return uprn;
    }

    // subscriber_test, D:\\TEMP\\COVID-Jiras\\08_July_2020_CQC_directory_NEL8.csv, C:\\Users\\PaulSimon\\Desktop\\builder.txt
    public static void OrganizationBuilder(String configName, String filename, String builderFilename) throws Exception
    {

        Connection connection = ConnectionManager.getSubscriberTransformConnection(configName);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"));

        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        Integer ft = 1; String RESOURCE_GUID = "";

        FileWriter csvWriter = new FileWriter(builderFilename);

        String csv = "";
        for (CSVRecord csvRecord : csvParser) {

            if (ft.equals(1)) {ft=0; continue;} // skip headers

            String cqc_id = csvRecord.get(0);
            String name = csvRecord.get(1);
            String also = csvRecord.get(2);
            String cqc_address = csvRecord.get(3);
            String postcode = csvRecord.get(4);
            String phone = csvRecord.get(5);
            String service_web_site = csvRecord.get(6);
            String service_type = csvRecord.get(7);
            String date_of_last_check = csvRecord.get(8);
            String specialism = csvRecord.get(9);
            String provider_name = csvRecord.get(10);
            String local_authority = csvRecord.get(11);
            String region = csvRecord.get(12);
            String location_url = csvRecord.get(13);
            String cqc_location = csvRecord.get(14);
            String cqc_provider_id = csvRecord.get(15);
            String on_ratings = csvRecord.get(16);

            String uprn = getUPRN(name+","+cqc_address+","+postcode, "cqc`"+cqc_id);

            String ods_address = FindODSCodeUsingUprn(postcode, "ecarehomesite", uprn, name, configName);

            String ods_code = ""; String addline1=""; String addline2=""; String addline3=""; String addline4=""; String addline5="";
            String parent_organization = ""; String parent_current = ""; String opendate = ""; String closedate = "";

            if (!ods_address.isEmpty()) {
                String[] ss = ods_address.split("\\~",-1);
                ods_code = ss[1]; addline1 = ss[2]; addline2 = ss[3]; addline3 = ss[4]; addline4 = ss[5]; addline5 = ss[6];
                parent_organization = ss[9]; parent_current = ss[10];
                opendate = ss[11]; closedate = ss[12];
            }

            // get the address lines form the cqc file (but no way of working out district/county)
            System.out.println(cqc_address);

            if (ods_code.isEmpty()) {
                String[] ss = cqc_address.split("\\,",-1);
                if (indexInBound(ss, 0)) addline1 = ss[0].trim();
                if (indexInBound(ss, 1)) addline2 = ss[1].trim();
                if (indexInBound(ss, 2)) addline3 = ss[2].trim();
            }

            // >>> VLTVL~107 NEAVE CRESCENT~HAROLD HILL~~ROMFORD~ESSEX~RM3 8HW

            OrganizationBuilder organizationBuilder = new OrganizationBuilder();

            if (!ods_code.isEmpty()) {
                System.out.println(ods_code);
                organizationBuilder.setOdsCode(ods_code);
            }

            organizationBuilder.setName(name);

            AddressBuilder addressBuilder = new AddressBuilder(organizationBuilder);
            addressBuilder.addLine(addline1);
            addressBuilder.addLine(addline2);
            addressBuilder.addLine(addline3);

            if (!ods_code.isEmpty()) {
                addressBuilder.setCity(addline4);
                addressBuilder.setDistrict(addline5);
            }

            addressBuilder.setPostcode(postcode);

            createContactPoint(ContactPoint.ContactPointSystem.PHONE, phone, organizationBuilder);

            if (!cqc_id.isEmpty()) createContainedCoded(organizationBuilder,"ID",cqc_id);
            if (!name.isEmpty()) createContainedCoded(organizationBuilder,"name",name);
            if (!also.isEmpty()) createContainedCoded(organizationBuilder,"also_known_as",also);
            if (!service_web_site.isEmpty()) createContainedCoded(organizationBuilder,"services_web_site",service_web_site);
            if (!date_of_last_check.isEmpty()) createContainedCoded(organizationBuilder, "date_of_last_check", date_of_last_check);
            if (!provider_name.isEmpty()) createContainedCoded(organizationBuilder, "provider_name", provider_name);
            if (!local_authority.isEmpty()) createContainedCoded(organizationBuilder,"local_authority",local_authority);
            if (!region.isEmpty()) createContainedCoded(organizationBuilder,"region",region);
            if (!location_url.isEmpty()) createContainedCoded(organizationBuilder,"location_url",location_url);
            if (!cqc_location.isEmpty()) createContainedCoded(organizationBuilder,"cqc_location",cqc_location);
            if (!cqc_provider_id.isEmpty()) createContainedCoded(organizationBuilder,"cqc_provider_id",cqc_provider_id);
            if (!on_ratings.isEmpty()) createContainedCoded(organizationBuilder,"on_ratings",on_ratings);
            if (!service_type.isEmpty()) createContainedCoded(organizationBuilder,"service_type",service_type);
            if (!specialism.isEmpty()) createContainedCoded(organizationBuilder,"specialisms/services",specialism);
            // ods stuff
            if (!parent_organization.isEmpty()) createContainedCoded(organizationBuilder,"parent_organization",parent_organization);
            if (!parent_current.isEmpty()) createContainedCoded(organizationBuilder,"parent_current",parent_current);
            if (!opendate.isEmpty()) createContainedCoded(organizationBuilder,"open_date",opendate);
            if (!closedate.isEmpty()) createContainedCoded(organizationBuilder,"close_date",closedate);

            //RESOURCE_GUID = scratch(cqc_id, configName);
            RESOURCE_GUID = scratch(cqc_location, configName);

            organizationBuilder.setId(RESOURCE_GUID);

            String json = OrganisationTransformer_v2.Test(organizationBuilder);
            System.out.println(json);

            csvWriter.write(RESOURCE_GUID + "\t"+ json + "\n");
        }
        csvWriter.flush();
        csvWriter.close();
    }

    private static void createContainedCoded(HasContainedParametersI organizationBuilder, String property, String value) throws Exception
    {
        ContainedParametersBuilder containedParametersBuilder = new ContainedParametersBuilder(organizationBuilder);

        MapColumnRequest propertyRequest = new MapColumnRequest(
                "CM_Org_CQC", "CM_Sys_CQC", "CQC", "CQC",
                property
        );
        MapResponse propertyResponse = IMHelper.getIMMappedPropertyResponse(propertyRequest);

        MapColumnValueRequest valueRequest = new MapColumnValueRequest(
                "CM_Org_CQC", "CM_Sys_CQC", "CQC", "CQC",
                property, value
        );
        MapResponse valueResponse = IMHelper.getIMMappedPropertyValueResponse(valueRequest);

        CodeableConcept ccValue = new CodeableConcept();
        ccValue.addCoding().setCode(valueResponse.getConcept().getCode())
                .setSystem(valueResponse.getConcept().getScheme())
                .setDisplay(value+"~"+property); // include the property_name
        containedParametersBuilder.addParameter(propertyResponse.getConcept().getCode(), ccValue);
    }

    private static void createContactPoint(ContactPoint.ContactPointSystem system, String contactCell, HasContactPointI parentBuilder) throws Exception {

        ContactPointBuilder contactPointBuilder = new ContactPointBuilder(parentBuilder);
        contactPointBuilder.setSystem(system);
        contactPointBuilder.setUse(ContactPoint.ContactPointUse.WORK);
        contactPointBuilder.setValue(contactCell);
    }

    /*
    private static Long GetNextMap3Id(Connection connection)  throws Exception
    {
        String sql = "SELECT MAX(subscriber_id) FROM subscriber_id_map_3";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        Long max = 0L;
        if (rs.next()) {
            max = rs.getLong(1);
            max++;
            System.out.println(max);
        }
        ps.close();
        return max+100;
    }
     */

    private static String ResourceExist(String cqc_id, Connection connection) throws Exception
    {
        String sql = "select guid from cqc_id_map where cqc_id='"+cqc_id+"'";
        PreparedStatement preparedStmt = null;
        preparedStmt = connection.prepareStatement(sql);
        ResultSet rs = preparedStmt.executeQuery();
        String scratch_guid="";
        if (rs.next()) {scratch_guid = rs.getString(1);}
        preparedStmt.close();
        return scratch_guid;
    }

    private static String scratch(String cqc_id, String configName) throws Exception
    {

        //PseudoIdDalI pseudoIdDal = DalProvider.factoryPseudoIdDal("subscriber_test");
        //pseudoIdDal.PSTest();

        //OrganisationTransformer_v2.TestInsert();

        Connection connection = ConnectionManager.getSubscriberTransformConnection(configName);

        String sql = "";
        String scratch_guid = ResourceExist(cqc_id, connection);

        if (scratch_guid.isEmpty()) {scratch_guid = UUID.randomUUID().toString();}

        System.out.println(scratch_guid);
        // check that the guid is in subscriber_id_map_3
        sql = "SELECT * FROM subscriber_id_map_3 where subscriber_table=33 and source_id='Organization/"+scratch_guid+"'";
        PreparedStatement preparedStmt = connection.prepareStatement(sql);
        ResultSet rs = preparedStmt.executeQuery();
        if (rs.next()) {
            System.out.println(rs.getString(1));
            preparedStmt.close();
            connection.close();
            return scratch_guid;
        }
        preparedStmt.close();

        // subscriber_Id_map subscriber_id is auto incremental, so don't need this code!
        //Long id = GetNextMap3Id(connection);
        //System.out.println(id);

        sql = "INSERT INTO subscriber_id_map_3 (subscriber_table,source_id) values (?,?)";
        preparedStmt = null;
        preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1,"33");
        //preparedStmt.setString(2,id.toString());
        preparedStmt.setString(2,"Organization/"+scratch_guid);
        preparedStmt.executeUpdate();
        connection.commit();
        preparedStmt.close();

        // insert into scratch_v2 and subscriber_id_map_3
        sql = "insert into cqc_id_map(cqc_id, guid) values(?,?);";
        preparedStmt = null;
        preparedStmt = connection.prepareStatement(sql);
        preparedStmt.setString(1,cqc_id);
        preparedStmt.setString(2,scratch_guid);
        preparedStmt.executeUpdate();
        connection.commit();
        preparedStmt.close();

        connection.close();

        return scratch_guid;
    }
}