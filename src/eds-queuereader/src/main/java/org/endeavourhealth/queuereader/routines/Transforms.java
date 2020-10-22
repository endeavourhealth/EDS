package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.*;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.exceptions.TransformException;
import org.endeavourhealth.transform.common.*;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Transforms extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(Transforms.class);

    private static void createEmisDataTable(String fileType) throws Exception {

        ParserI parser = createParserForEmisFileType(fileType, null);
        if (parser == null) {
            return;
        }

        System.out.println("-- " + fileType);

        String table = fileType.replace(" ", "_");

        String dropSql = "DROP TABLE IF EXISTS `" + table + "`;";
        System.out.println(dropSql);

        String sql = "CREATE TABLE `" + table + "` (";

        sql += "file_name varchar(100)";
        sql += ", ";
        sql += "extract_date datetime";

        if (parser instanceof AbstractFixedParser) {

            AbstractFixedParser fixedParser = (AbstractFixedParser) parser;
            List<FixedParserField> fields = fixedParser.getFieldList();

            for (FixedParserField field : fields) {
                String col = field.getName();
                int len = field.getFieldlength();
                sql += ", ";
                sql += col.replace(" ", "_").replace("#", "").replace("/", "");
                sql += " varchar(";
                sql += len;
                sql += ")";
            }

        } else {

            List<String> cols = parser.getColumnHeaders();
            for (String col : cols) {
                sql += ", ";
                sql += col.replace(" ", "_").replace("#", "").replace("/", "");

                if (col.equals("BLOB_CONTENTS")
                        || col.equals("VALUE_LONG_TXT")
                        || col.equals("COMMENT_TXT")
                        || col.equals("NONPREG_REL_PROBLM_SCT_CD")) {

                    sql += " mediumtext";

                } else if (col.indexOf("Date") > -1
                        || col.indexOf("Time") > -1) {
                    sql += " varchar(10)";

                } else {
                    sql += " varchar(255)";
                }
            }
        }

        sql += ");";
		/*LOG.debug("-- fileType");
		LOG.debug(sql);*/
        System.out.println(sql);
    }


    private static ParserI createParserForEmisFileType(String fileType, String filePath) {

        String[] toks = fileType.split("_");

        String domain = toks[0];
        String name = toks[1];

        String first = domain.substring(0, 1);
        String last = domain.substring(1);
        domain = first.toLowerCase() + last;

        try {
            String clsName = "org.endeavourhealth.transform.emis.csv.schema." + domain + "." + name;
            Class cls = Class.forName(clsName);

            //now construct an instance of the parser for the file we've found
            Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
            return constructor.newInstance(null, null, null, EmisCsvToFhirTransformer.VERSION_5_4, filePath);

        } catch (Exception ex) {
            LOG.error("No parser for file type [" + fileType + "]");
            LOG.error("", ex);
            return null;
        }
    }

    private static void loadEmisDataFromFile(Connection conn, String filePath, String fileType, Date extractDate) throws Exception {
        LOG.debug("Loading " + fileType + ": " + filePath);

        String fileName = FilenameUtils.getName(filePath);

        ParserI parser = createParserForEmisFileType(fileType, filePath);
        if (parser == null) {
            return;
        }

        String table = fileType.replace(" ", "_");

        //check table is there
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_schema = database() AND table_name = '" + table + "' LIMIT 1";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        boolean tableExists = rs.next();
        rs.close();
        statement.close();

        if (!tableExists) {
            LOG.error("No table exists for " + table);
            return;
        }

        //create insert statement
        sql = "INSERT INTO `" + table + "` (";
        sql += "file_name, extract_date";
        List<String> cols = parser.getColumnHeaders();
        for (String col : cols) {
            sql += ", ";
            sql += col.replace(" ", "_").replace("#", "").replace("/", "");
        }
        sql += ") VALUES (";
        sql += "?, ?";
        for (String col : cols) {
            sql += ", ";
            sql += "?";
        }
        sql += ")";
        PreparedStatement ps = conn.prepareStatement(sql);

        List<String> currentBatchStrs = new ArrayList<>();

        //load table
        try {
            int done = 0;
            int currentBatchSize = 0;
            while (parser.nextRecord()) {

                int col = 1;

                //file name is always first
                ps.setString(col++, fileName);
                ps.setDate(col++, new java.sql.Date(extractDate.getTime()));

                for (String colName : cols) {
                    CsvCell cell = parser.getCell(colName);
                    if (cell == null) {
                        ps.setNull(col++, Types.VARCHAR);
                    } else {
                        ps.setString(col++, cell.getString());
                    }
                }

                ps.addBatch();
                currentBatchSize++;
                currentBatchStrs.add((ps.toString())); //for error handling

                if (currentBatchSize >= 5) {
                    ps.executeBatch();
                    currentBatchSize = 0;
                    currentBatchStrs.clear();
                }

                done++;
                if (done % 5000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            if (currentBatchSize >= 0) {
                ps.executeBatch();
            }

            ps.close();
        } catch (Throwable t) {
            LOG.error("Failed on batch with statements:");
            for (String currentBatchStr : currentBatchStrs) {
                LOG.error(currentBatchStr);
            }
            throw t;
        }

        LOG.debug("Finished " + fileType + ": " + filePath);
    }

    public static void createBartsDataTables() {
        LOG.debug("Creating Barts data tables");
        try {
            List<String> fileTypes = new ArrayList<>();
            fileTypes.add("AEATT");
            fileTypes.add("Birth");
            //fileTypes.add("BulkDiagnosis");
            //fileTypes.add("BulkProblem");
            //fileTypes.add("BulkProcedure");
            fileTypes.add("CLEVE");
            fileTypes.add("CVREF");
            fileTypes.add("DIAGN");
            fileTypes.add("Diagnosis");
            fileTypes.add("ENCINF");
            fileTypes.add("ENCNT");
            fileTypes.add("FamilyHistory");
            fileTypes.add("IPEPI");
            fileTypes.add("IPWDS");
            fileTypes.add("LOREF");
            fileTypes.add("NOMREF");
            fileTypes.add("OPATT");
            fileTypes.add("ORDER");
            fileTypes.add("ORGREF");
            fileTypes.add("PPADD");
            fileTypes.add("PPAGP");
            fileTypes.add("PPALI");
            fileTypes.add("PPATI");
            fileTypes.add("PPINF");
            fileTypes.add("PPNAM");
            fileTypes.add("PPPHO");
            fileTypes.add("PPREL");
            fileTypes.add("Pregnancy");
            fileTypes.add("Problem");
            fileTypes.add("PROCE");
            fileTypes.add("Procedure");
            fileTypes.add("PRSNLREF");
            fileTypes.add("SusEmergency");
            fileTypes.add("SusInpatient");
            fileTypes.add("SusOutpatient");
            fileTypes.add("EventCode");
            fileTypes.add("EventSetCanon");
            fileTypes.add("EventSet");
            fileTypes.add("EventSetExplode");
            fileTypes.add("BlobContent");
            fileTypes.add("SusInpatientTail");
            fileTypes.add("SusOutpatientTail");
            fileTypes.add("SusEmergencyTail");
            fileTypes.add("AEINV");
            fileTypes.add("AETRE");
            fileTypes.add("OPREF");
            fileTypes.add("STATREF");
            fileTypes.add("RTTPE");
            fileTypes.add("PPATH");
            fileTypes.add("DOCRP");
            fileTypes.add("SCHAC");
            fileTypes.add("EALEN");
            fileTypes.add("DELIV");
            fileTypes.add("EALOF");
            fileTypes.add("SusEmergencyCareDataSet");
            fileTypes.add("SusEmergencyCareDataSetTail");


            for (String fileType : fileTypes) {
                createBartsDataTable(fileType);
            }

            LOG.debug("Finished Creating Barts data tables");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createBartsDataTable(String fileType) throws Exception {

        ParserI parser = null;
        try {
            String clsName = "org.endeavourhealth.transform.barts.schema." + fileType;
            Class cls = Class.forName(clsName);

            //now construct an instance of the parser for the file we've found
            Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
            parser = constructor.newInstance(null, null, null, null, null);

        } catch (ClassNotFoundException cnfe) {
            System.out.println("-- No parser for file type [" + fileType + "]");
            return;
        }

        System.out.println("-- " + fileType);

        String table = fileType.replace(" ", "_");

        String dropSql = "DROP TABLE IF EXISTS `" + table + "`;";
        System.out.println(dropSql);

        String sql = "CREATE TABLE `" + table + "` (";

        sql += "file_name varchar(100)";

        if (parser instanceof AbstractFixedParser) {

            AbstractFixedParser fixedParser = (AbstractFixedParser) parser;
            List<FixedParserField> fields = fixedParser.getFieldList();

            for (FixedParserField field : fields) {
                String col = field.getName();
                int len = field.getFieldlength();
                sql += ", ";
                sql += col.replace(" ", "_").replace("#", "").replace("/", "");
                sql += " varchar(";
                sql += len;
                sql += ")";
            }

        } else {

            List<String> cols = parser.getColumnHeaders();
            for (String col : cols) {
                sql += ", ";
                sql += col.replace(" ", "_").replace("#", "").replace("/", "");

                if (col.equals("BLOB_CONTENTS")
                        || col.equals("VALUE_LONG_TXT")
                        || col.equals("COMMENT_TXT")
                        || col.equals("NONPREG_REL_PROBLM_SCT_CD")
                        || col.equals("ORDER_COMMENTS_TXT")) {

                    sql += " mediumtext";

                } else if (col.indexOf("Date") > -1
                        || col.indexOf("Time") > -1) {
                    sql += " varchar(10)";

                } else {
                    sql += " varchar(255)";
                }
            }
        }

        sql += ");";
		/*LOG.debug("-- fileType");
		LOG.debug(sql);*/
        System.out.println(sql);

    }

    public static void loadBartsData(String serviceId, String systemId, String dbUrl, String dbUsername, String dbPassword, String startDateStr, String onlyThisFileType) {
        LOG.debug("Loading Barts data from into " + dbUrl);
        try {
            //hash file type of every file
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(UUID.fromString(serviceId), UUID.fromString(systemId), Integer.MAX_VALUE);

            //open connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(startDateStr);

            for (int i = exchanges.size() - 1; i >= 0; i--) {
                Exchange exchange = exchanges.get(i);
                String exchangeBody = exchange.getBody();
                List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

                if (files.isEmpty()) {
                    continue;
                }

                for (ExchangePayloadFile file : files) {
                    String type = file.getType();
                    String path = file.getPath();

                    //if only doing a specific file type, skip all others
                    if (onlyThisFileType != null
                            && !type.equals(onlyThisFileType)) {
                        continue;
                    }

                    boolean processFile = false;
                    if (type.equalsIgnoreCase("CVREF")
                            || type.equalsIgnoreCase("LOREF")
                            || type.equalsIgnoreCase("ORGREF")
                            || type.equalsIgnoreCase("PRSNLREF")
                            || type.equalsIgnoreCase("NOMREF")) {
                        processFile = true;

                    } else {

                        File f = new File(path);
                        File parentFile = f.getParentFile();
                        String parentDir = parentFile.getName();
                        Date extractDate = sdf.parse(parentDir);
                        if (!extractDate.before(startDate)) {
                            processFile = true;
                        }
						/*if (!extractDate.before(startDate)
								&& !extractDate.after(endDate)) {
							processFile = true;
						}*/
                    }

                    if (processFile) {
                        loadBartsDataFromFile(conn, path, type);
                    }
                }
            }

            conn.close();

            LOG.debug("Finished Loading Barts data from into " + dbUrl);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void loadBartsDataFromFile(Connection conn, String filePath, String fileType) throws Exception {
        LOG.debug("Loading " + fileType + ": " + filePath);

        String fileName = FilenameUtils.getName(filePath);

        ParserI parser = null;
        try {
            String clsName = "org.endeavourhealth.transform.barts.schema." + fileType;
            Class cls = Class.forName(clsName);

            //now construct an instance of the parser for the file we've found
            Constructor<AbstractCsvParser> constructor = cls.getConstructor(UUID.class, UUID.class, UUID.class, String.class, String.class);
            parser = constructor.newInstance(null, null, null, null, filePath);

        } catch (ClassNotFoundException cnfe) {
            LOG.error("No parser for file type [" + fileType + "]");
            return;
        }

        String table = fileType.replace(" ", "_");

        //check table is there
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_schema = database() AND table_name = '" + table + "' LIMIT 1";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        boolean tableExists = rs.next();
        rs.close();
        statement.close();

        if (!tableExists) {
            LOG.error("No table exists for " + table);
            return;
        }

        //create insert statement
        sql = "INSERT INTO `" + table + "` (";
        sql += "file_name";
        List<String> cols = parser.getColumnHeaders();
        for (String col : cols) {
            sql += ", ";
            sql += col.replace(" ", "_").replace("#", "").replace("/", "");
        }
        sql += ") VALUES (";
        sql += "?";
        for (String col : cols) {
            sql += ", ";
            sql += "?";
        }
        sql += ")";
        PreparedStatement ps = conn.prepareStatement(sql);

        List<String> currentBatchStrs = new ArrayList<>();

        //load table
        try {
            int done = 0;
            int currentBatchSize = 0;
            while (parser.nextRecord()) {

                int col = 1;

                //file name is always first
                ps.setString(col++, fileName);

                for (String colName : cols) {
                    CsvCell cell = parser.getCell(colName);
                    if (cell == null) {
                        ps.setNull(col++, Types.VARCHAR);
                    } else {
                        ps.setString(col++, cell.getString());
                    }
                }

                ps.addBatch();
                currentBatchSize++;
                currentBatchStrs.add((ps.toString())); //for error handling

                if (currentBatchSize >= 5) {
                    ps.executeBatch();
                    currentBatchSize = 0;
                    currentBatchStrs.clear();
                }

                done++;
                if (done % 5000 == 0) {
                    LOG.debug("Done " + done);
                }
            }

            if (currentBatchSize >= 0) {
                ps.executeBatch();
            }

            ps.close();
        } catch (Throwable t) {
            LOG.error("Failed on batch with statements:");
            for (String currentBatchStr : currentBatchStrs) {
                LOG.error(currentBatchStr);
            }
            throw t;
        }

        LOG.debug("Finished " + fileType + ": " + filePath);
    }


    public static void createEmisDataTables() {
        LOG.debug("Creating Emis data tables");
        try {
            List<String> fileTypes = new ArrayList<>();

            fileTypes.add("Admin_Location");
            fileTypes.add("Admin_OrganisationLocation");
            fileTypes.add("Admin_Organisation");
            fileTypes.add("Admin_Patient");
            fileTypes.add("Admin_UserInRole");
            fileTypes.add("Agreements_SharingOrganisation");
            fileTypes.add("Appointment_SessionUser");
            fileTypes.add("Appointment_Session");
            fileTypes.add("Appointment_Slot");
            fileTypes.add("CareRecord_Consultation");
            fileTypes.add("CareRecord_Diary");
            fileTypes.add("CareRecord_ObservationReferral");
            fileTypes.add("CareRecord_Observation");
            fileTypes.add("CareRecord_Problem");
            fileTypes.add("Coding_ClinicalCode");
            fileTypes.add("Coding_DrugCode");
            fileTypes.add("Prescribing_DrugRecord");
            fileTypes.add("Prescribing_IssueRecord");
            fileTypes.add("Audit_PatientAudit");
            fileTypes.add("Audit_RegistrationAudit");

            for (String fileType : fileTypes) {
                createEmisDataTable(fileType);
            }

            LOG.debug("Finished Creating Emis data tables");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }


    public static void createAdastraSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
        LOG.info("Creating Adastra Subset");

        try {

            Set<String> caseIds = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
            for (String line : lines) {
                line = line.trim();

                //ignore comments
                if (line.startsWith("#")) {
                    continue;
                }

                //adastra extract files are all keyed on caseId
                caseIds.add(line);
            }

            File sourceDir = new File(sourceDirPath);
            File destDir = new File(destDirPath);

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            createAdastraSubsetForFile(sourceDir, destDir, caseIds);

            LOG.info("Finished Creating Adastra Subset");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createAdastraSubsetForFile(File sourceDir, File destDir, Set<String> caseIds) throws Exception {

        File[] files = sourceDir.listFiles();
        LOG.info("Found " + files.length + " files in " + sourceDir);

        for (File sourceFile : files) {

            String name = sourceFile.getName();
            File destFile = new File(destDir, name);

            if (sourceFile.isDirectory()) {

                if (!destFile.exists()) {
                    destFile.mkdirs();
                }

                createAdastraSubsetForFile(sourceFile, destFile, caseIds);

            } else {

                if (destFile.exists()) {
                    destFile.delete();
                }

                LOG.info("Checking file " + sourceFile);

                //skip any non-CSV file
                String ext = FilenameUtils.getExtension(name);
                if (!ext.equalsIgnoreCase("csv")) {
                    LOG.info("Skipping as not a CSV file");
                    continue;
                }

                FileReader fr = new FileReader(sourceFile);
                BufferedReader br = new BufferedReader(fr);

                //fully quote destination file to fix CRLF in columns
                CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');

                CSVParser parser = new CSVParser(br, format);

                int filterColumn = -1;

                //CaseRef column at 0
                if (name.contains("NOTES") || name.contains("CASEQUESTIONS") ||
                        name.contains("OUTCOMES") || name.contains("CONSULTATION") ||
                        name.contains("CLINICALCODES") || name.contains("PRESCRIPTIONS") ||
                        name.contains("PATIENT")) {

                    filterColumn = 0;

                } else if (name.contains("CASE")) {
                    //CaseRef column at 2
                    filterColumn = 2;

                } else if (name.contains("PROVIDER")) {
                    //CaseRef column at 7
                    filterColumn = 7;

                } else {
                    //if no patient column, just copy the file
                    parser.close();

                    LOG.info("Copying non-patient file " + sourceFile);
                    copyFile(sourceFile, destFile);
                    continue;
                }

                PrintWriter fw = new PrintWriter(destFile);
                BufferedWriter bw = new BufferedWriter(fw);

                CSVPrinter printer = new CSVPrinter(bw, format);

                Iterator<CSVRecord> csvIterator = parser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();

                    String caseId = csvRecord.get(filterColumn);
                    if (caseIds.contains(caseId)) {

                        printer.printRecord(csvRecord);
                        printer.flush();
                    }
                }

                parser.close();
                printer.close();
            }
        }
    }


    public static void createEmisSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
        LOG.info("Creating Emis Subset");

        try {

            Set<String> patientGuids = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
            for (String line : lines) {
                line = line.trim();

                //ignore comments
                if (line.startsWith("#")) {
                    continue;
                }
                patientGuids.add(line);
            }

            File sourceDir = new File(sourceDirPath);
            File destDir = new File(destDirPath);

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            createEmisSubsetForFile(sourceDir, destDir, patientGuids);

            LOG.info("Finished Creating Emis Subset");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createEmisSubsetForFile(File sourceDir, File destDir, Set<String> patientGuids) throws Exception {

        File[] files = sourceDir.listFiles();
        LOG.info("Found " + files.length + " files in " + sourceDir);

        for (File sourceFile : files) {

            String name = sourceFile.getName();
            File destFile = new File(destDir, name);

            if (sourceFile.isDirectory()) {

                if (!destFile.exists()) {
                    destFile.mkdirs();
                }

                createEmisSubsetForFile(sourceFile, destFile, patientGuids);

            } else {

                if (destFile.exists()) {
                    destFile.delete();
                }

                LOG.info("Checking file " + sourceFile);

                //skip any non-CSV file
                String ext = FilenameUtils.getExtension(name);
                if (!ext.equalsIgnoreCase("csv")) {
                    LOG.info("Skipping as not a CSV file");
                    continue;
                }

                CSVFormat format = CSVFormat.DEFAULT.withHeader();

                InputStreamReader reader = new InputStreamReader(
                        new BufferedInputStream(
                                new FileInputStream(sourceFile)));

                CSVParser parser = new CSVParser(reader, format);

                String filterColumn = null;

                Map<String, Integer> headerMap = parser.getHeaderMap();
                if (headerMap.containsKey("PatientGuid")) {
                    filterColumn = "PatientGuid";

                } else {
                    //if no patient column, just copy the file
                    parser.close();

                    LOG.info("Copying non-patient file " + sourceFile);
                    copyFile(sourceFile, destFile);
                    continue;
                }

                String[] columnHeaders = new String[headerMap.size()];
                Iterator<String> headerIterator = headerMap.keySet().iterator();
                while (headerIterator.hasNext()) {
                    String headerName = headerIterator.next();
                    int headerIndex = headerMap.get(headerName);
                    columnHeaders[headerIndex] = headerName;
                }

                BufferedWriter bw =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(destFile)));

                CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

                Iterator<CSVRecord> csvIterator = parser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();

                    String patientGuid = csvRecord.get(filterColumn);
                    if (Strings.isNullOrEmpty(patientGuid) //if empty, carry over this record
                            || patientGuids.contains(patientGuid)) {

                        printer.printRecord(csvRecord);
                        printer.flush();
                    }
                }

                parser.close();
                printer.close();
            }
        }
    }

    public static void createTppSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
        LOG.info("Creating TPP Subset");

        try {

            Set<String> personIds = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
            for (String line : lines) {
                line = line.trim();

                //ignore comments
                if (line.startsWith("#")) {
                    continue;
                }
                personIds.add(line);
            }

            File sourceDir = new File(sourceDirPath);
            File destDir = new File(destDirPath);

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            createTppSubsetForFile(sourceDir, destDir, personIds);

            LOG.info("Finished Creating TPP Subset");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createTppSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

        File[] files = sourceDir.listFiles();
        LOG.info("Found " + files.length + " files in " + sourceDir);

        for (File sourceFile : files) {

            String name = sourceFile.getName();
            File destFile = new File(destDir, name);

            if (sourceFile.isDirectory()) {

                if (!destFile.exists()) {
                    destFile.mkdirs();
                }

                //LOG.info("Doing dir " + sourceFile);
                createTppSubsetForFile(sourceFile, destFile, personIds);

            } else {

                if (destFile.exists()) {
                    destFile.delete();
                }

                LOG.info("Checking file " + sourceFile);

                //skip any non-CSV file
                String ext = FilenameUtils.getExtension(name);
                if (!ext.equalsIgnoreCase("csv")) {
                    LOG.info("Skipping as not a CSV file");
                    continue;
                }

                Charset encoding = Charset.forName("CP1252");
                InputStreamReader reader =
                        new InputStreamReader(
                                new BufferedInputStream(
                                        new FileInputStream(sourceFile)), encoding);

                CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withHeader();

                CSVParser parser = new CSVParser(reader, format);

                String filterColumn = null;

                Map<String, Integer> headerMap = parser.getHeaderMap();
                if (headerMap.containsKey("IDPatient")) {
                    filterColumn = "IDPatient";

                } else if (name.equalsIgnoreCase("SRPatient.csv")) {
                    filterColumn = "RowIdentifier";

                } else {
                    //if no patient column, just copy the file
                    parser.close();

                    LOG.info("Copying non-patient file " + sourceFile);
                    copyFile(sourceFile, destFile);
                    continue;
                }

                String[] columnHeaders = new String[headerMap.size()];
                Iterator<String> headerIterator = headerMap.keySet().iterator();
                while (headerIterator.hasNext()) {
                    String headerName = headerIterator.next();
                    int headerIndex = headerMap.get(headerName);
                    columnHeaders[headerIndex] = headerName;
                }

                BufferedWriter bw =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(destFile), encoding));

                CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

                Iterator<CSVRecord> csvIterator = parser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();

                    String patientId = csvRecord.get(filterColumn);
                    if (personIds.contains(patientId)) {

                        printer.printRecord(csvRecord);
                        printer.flush();
                    }
                }

                parser.close();
                printer.close();

				/*} else {
					//the 2.1 files are going to be a pain to split by patient, so just copy them over
					LOG.info("Copying 2.1 file " + sourceFile);
					copyFile(sourceFile, destFile);
				}*/
            }
        }
    }

    public static void createVisionSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
        LOG.info("Creating Vision Subset");

        try {

            Set<String> personIds = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
            for (String line : lines) {
                line = line.trim();

                //ignore comments
                if (line.startsWith("#")) {
                    continue;
                }
                personIds.add(line);
            }

            File sourceDir = new File(sourceDirPath);
            File destDir = new File(destDirPath);

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            createVisionSubsetForFile(sourceDir, destDir, personIds);

            LOG.info("Finished Creating Vision Subset");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createVisionSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

        File[] files = sourceDir.listFiles();
        LOG.info("Found " + files.length + " files in " + sourceDir);

        for (File sourceFile : files) {

            String name = sourceFile.getName();
            File destFile = new File(destDir, name);

            if (sourceFile.isDirectory()) {

                if (!destFile.exists()) {
                    destFile.mkdirs();
                }

                createVisionSubsetForFile(sourceFile, destFile, personIds);

            } else {

                if (destFile.exists()) {
                    destFile.delete();
                }

                LOG.info("Checking file " + sourceFile);

                //skip any non-CSV file
                String ext = FilenameUtils.getExtension(name);
                if (!ext.equalsIgnoreCase("csv")) {
                    LOG.info("Skipping as not a CSV file");
                    continue;
                }

                FileReader fr = new FileReader(sourceFile);
                BufferedReader br = new BufferedReader(fr);

                CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL);

                CSVParser parser = new CSVParser(br, format);

                int filterColumn = -1;

                if (name.contains("encounter_data") || name.contains("journal_data") ||
                        name.contains("patient_data") || name.contains("referral_data")) {

                    filterColumn = 0;
                } else {
                    //if no patient column, just copy the file
                    parser.close();

                    LOG.info("Copying non-patient file " + sourceFile);
                    copyFile(sourceFile, destFile);
                    continue;
                }

                PrintWriter fw = new PrintWriter(destFile);
                BufferedWriter bw = new BufferedWriter(fw);

                CSVPrinter printer = new CSVPrinter(bw, format);

                Iterator<CSVRecord> csvIterator = parser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();

                    String patientId = csvRecord.get(filterColumn);
                    if (personIds.contains(patientId)) {

                        printer.printRecord(csvRecord);
                        printer.flush();
                    }
                }

                parser.close();
                printer.close();
            }
        }
    }

    public static void createHomertonSubset(String sourceDirPath, String destDirPath, String samplePatientsFile) {
        LOG.info("Creating Homerton Subset");

        try {

            Set<String> PersonIds = new HashSet<>();
            List<String> lines = Files.readAllLines(new File(samplePatientsFile).toPath());
            for (String line : lines) {
                line = line.trim();

                //ignore comments
                if (line.startsWith("#")) {
                    continue;
                }

                PersonIds.add(line);
            }

            File sourceDir = new File(sourceDirPath);
            File destDir = new File(destDirPath);

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            createHomertonSubsetForFile(sourceDir, destDir, PersonIds);

            LOG.info("Finished Creating Homerton Subset");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void createHomertonSubsetForFile(File sourceDir, File destDir, Set<String> personIds) throws Exception {

        File[] files = sourceDir.listFiles();
        LOG.info("Found " + files.length + " files in " + sourceDir);

        for (File sourceFile : files) {

            String name = sourceFile.getName();
            File destFile = new File(destDir, name);

            if (sourceFile.isDirectory()) {

                if (!destFile.exists()) {
                    destFile.mkdirs();
                }

                createHomertonSubsetForFile(sourceFile, destFile, personIds);

            } else {

                if (destFile.exists()) {
                    destFile.delete();
                }

                LOG.info("Checking file " + sourceFile);

                //skip any non-CSV file
                String ext = FilenameUtils.getExtension(name);
                if (!ext.equalsIgnoreCase("csv")) {
                    LOG.info("Skipping as not a CSV file");
                    continue;
                }

                FileReader fr = new FileReader(sourceFile);
                BufferedReader br = new BufferedReader(fr);

                //fully quote destination file to fix CRLF in columns
                CSVFormat format = CSVFormat.DEFAULT.withHeader();

                CSVParser parser = new CSVParser(br, format);

                int filterColumn = -1;

                //PersonId column at 1
                if (name.contains("ENCOUNTER") || name.contains("PATIENT")) {
                    filterColumn = 1;

                } else if (name.contains("DIAGNOSIS")) {
                    //PersonId column at 13
                    filterColumn = 13;
                } else if (name.contains("ALLERGY")) {
                    //PersonId column at 2
                    filterColumn = 2;

                } else if (name.contains("PROBLEM")) {
                    //PersonId column at 4
                    filterColumn = 4;
                } else {
                    //if no patient column, just copy the file (i.e. PROCEDURE)
                    parser.close();

                    LOG.info("Copying file without PatientId " + sourceFile);
                    copyFile(sourceFile, destFile);
                    continue;
                }

                Map<String, Integer> headerMap = parser.getHeaderMap();
                String[] columnHeaders = new String[headerMap.size()];
                Iterator<String> headerIterator = headerMap.keySet().iterator();
                while (headerIterator.hasNext()) {
                    String headerName = headerIterator.next();
                    int headerIndex = headerMap.get(headerName);
                    columnHeaders[headerIndex] = headerName;
                }

                PrintWriter fw = new PrintWriter(destFile);
                BufferedWriter bw = new BufferedWriter(fw);

                CSVPrinter printer = new CSVPrinter(bw, format.withHeader(columnHeaders));

                Iterator<CSVRecord> csvIterator = parser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();

                    String patientId = csvRecord.get(filterColumn);
                    if (personIds.contains(patientId)) {

                        printer.printRecord(csvRecord);
                        printer.flush();
                    }
                }

                parser.close();
                printer.close();
            }
        }
    }


    public static void loadEmisData(String serviceId, String systemId, String dbUrl, String dbUsername, String dbPassword, String onlyThisFileType) {
        LOG.debug("Loading Emis data from into " + dbUrl);
        try {
            //hash file type of every file
            ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
            List<Exchange> exchanges = exchangeDal.getExchangesByService(UUID.fromString(serviceId), UUID.fromString(systemId), Integer.MAX_VALUE);

            //open connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            SimpleDateFormat sdfStart = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdfStart.parse("2000-01-01");

            for (int i = exchanges.size() - 1; i >= 0; i--) {
                Exchange exchange = exchanges.get(i);
                String exchangeBody = exchange.getBody();
                List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(exchangeBody);

                if (files.isEmpty()) {
                    continue;
                }

                for (ExchangePayloadFile file : files) {
                    String type = file.getType();
                    String path = file.getPath();

                    //if only doing a specific file type, skip all others
                    if (onlyThisFileType != null
                            && !type.equals(onlyThisFileType)) {
                        continue;
                    }

                    String name = FilenameUtils.getBaseName(path);
                    String[] toks = name.split("_");
                    if (toks.length != 5) {
                        throw new TransformException("Failed to find extract date in filename " + path);
                    }
                    String dateStr = toks[3];
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date extractDate = sdf.parse(dateStr);

                    boolean processFile = false;

                    if (type.equalsIgnoreCase("OriginalTerms")
                            || type.equalsIgnoreCase("RegistrationStatus")) {
                        //can't process these custom files in this routine

                    } else if (type.equalsIgnoreCase("Coding_ClinicalCode")
                            || type.equalsIgnoreCase("Coding_DrugCode")) {
                        processFile = true;

                    } else {

                        if (!extractDate.before(startDate)) {
                            processFile = true;
                        }
                    }

                    if (processFile) {
                        loadEmisDataFromFile(conn, path, type, extractDate);
                    }
                }
            }

            conn.close();

            LOG.debug("Finished Emis data from into " + dbUrl);
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

}
