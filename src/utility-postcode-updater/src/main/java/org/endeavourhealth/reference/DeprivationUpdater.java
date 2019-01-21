package org.endeavourhealth.reference;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.reference.ReferenceUpdaterDalI;
import org.endeavourhealth.core.database.dal.reference.models.DeprivationLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;

public class DeprivationUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DeprivationUpdater.class);

    /**
     * updates the deprivation_lookup table in the reference DB from ONS data
     *
     * Usage
     * =================================================================================
     * 1. go to https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015
     * 2. download "File 7: all ranks, deciles and scores for the indices of deprivation, and population denominators"
     * 3. Then run this utility on that CSV file, as:
     *      Main deprivation <csv file>
     */
    public static void updateDeprivationScores(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: deprivation <deprivation CSV file>");
            return;
        }

        LOG.info("Deprivation Update Starting");

        File f = new File(args[1]);

        if (!f.exists()) {
            LOG.error("" + f + " doesn't exist");
        }

        LOG.info("Processing Deprivation file");
        processFile(f);
        LOG.info("Finished Deprivation file");
    }

    private static void processFile(File f) throws Exception {

        CSVFormat format = CSVFormat.DEFAULT;

        int rowsDone = 0;
        CSVParser parser = null;
        try {
            //the postcode CSV file doesn't contain headers, so we must just pass in the headers we know should be there
            parser = CSVParser.parse(f, Charset.defaultCharset(), format.withHeader());
            CsvHelper.validateCsvHeaders(parser, f.getAbsolutePath(), getCsvHeadings());

            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                updateDeprivationScore(record);

                rowsDone ++;
                if (rowsDone % 1000 == 0) {
                    LOG.info("Done " + rowsDone + " (of approx 33k)");
                }
            }

            LOG.info("Finshed at " + rowsDone + " rows");

        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static Double convertDouble(CSVRecord record, String column) {
        String s = record.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return Double.valueOf(s);
    }

    private static Integer convertInt(CSVRecord record, String column) {
        String s = record.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return Integer.valueOf(s);
    }

    private static void updateDeprivationScore(CSVRecord record) throws Exception {

        DeprivationLookup lookup = new DeprivationLookup();
        lookup.setLsoaCode(record.get("LSOA code (2011)"));

        lookup.setImdScore(convertDouble(record, "Index of Multiple Deprivation (IMD) Score"));
        lookup.setImdRank(convertInt(record, "Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)"));
        lookup.setImdDecile(convertInt(record, "Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setIncomeScore(convertDouble(record, "Income Score (rate)"));
        lookup.setIncomeRank(convertInt(record, "Income Rank (where 1 is most deprived)"));
        lookup.setIncomeDecile(convertInt(record, "Income Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setEmploymentScore(convertDouble(record, "Employment Score (rate)"));
        lookup.setEmploymentRank(convertInt(record, "Employment Rank (where 1 is most deprived)"));
        lookup.setEmploymentDecile(convertInt(record, "Employment Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setEducationScore(convertDouble(record, "Education, Skills and Training Score"));
        lookup.setEducationRank(convertInt(record, "Education, Skills and Training Rank (where 1 is most deprived)"));
        lookup.setEducationDecile(convertInt(record, "Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setHealthScore(convertDouble(record, "Health Deprivation and Disability Score"));
        lookup.setHealthRank(convertInt(record, "Health Deprivation and Disability Rank (where 1 is most deprived)"));
        lookup.setHealthDecile(convertInt(record, "Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setCrimeScore(convertDouble(record, "Crime Score"));
        lookup.setCrimeRank(convertInt(record, "Crime Rank (where 1 is most deprived)"));
        lookup.setCrimeDecile(convertInt(record, "Crime Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setHousingAndServicesBarriersScore(convertDouble(record, "Barriers to Housing and Services Score"));
        lookup.setHousingAndServicesBarriersRank(convertInt(record, "Barriers to Housing and Services Rank (where 1 is most deprived)"));
        lookup.setHousingAndServicesBarriersDecile(convertInt(record, "Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setLivingEnvironmentScore(convertDouble(record, "Living Environment Score"));
        lookup.setLivingEnvironmentRank(convertInt(record, "Living Environment Rank (where 1 is most deprived)"));
        lookup.setLivingEnvironmentDecile(convertInt(record, "Living Environment Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setIdaciScore(convertDouble(record, "Income Deprivation Affecting Children Index (IDACI) Score (rate)"));
        lookup.setIdaciRank(convertInt(record, "Income Deprivation Affecting Children Index (IDACI) Rank (where 1 is most deprived)"));
        lookup.setIdaciDecile(convertInt(record, "Income Deprivation Affecting Children Index (IDACI) Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setIdaopiScore(convertDouble(record, "Income Deprivation Affecting Older People (IDAOPI) Score (rate)"));
        lookup.setIdaopiRank(convertInt(record, "Income Deprivation Affecting Older People (IDAOPI) Rank (where 1 is most deprived)"));
        lookup.setIdaopiDecile(convertInt(record, "Income Deprivation Affecting Older People (IDAOPI) Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setChildrenAndYoungSubDomainScore(convertDouble(record, "Children and Young People Sub-domain Score"));
        lookup.setChildrenAndYoungSubDomainRank(convertInt(record, "Children and Young People Sub-domain Rank (where 1 is most deprived)"));
        lookup.setChildrenAndYoungSubDomainDecile(convertInt(record, "Children and Young People Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setAdultSkillsSubDomainScore(convertDouble(record, "Adult Skills Sub-domain Score"));
        lookup.setAdultSkillsSubDomainRank(convertInt(record, "Adult Skills Sub-domain Rank (where 1 is most deprived)"));
        lookup.setAdultSkillsSubDomainDecile(convertInt(record, "Adult Skills Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setGeographicalBarriersSubDomainScore(convertDouble(record, "Geographical Barriers Sub-domain Score"));
        lookup.setGeographicalBarriersSubDomainRank(convertInt(record, "Geographical Barriers Sub-domain Rank (where 1 is most deprived)"));
        lookup.setGeographicalBarriersSubDomainDecile(convertInt(record, "Geographical Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setWiderBarriersSubDomainScore(convertDouble(record, "Wider Barriers Sub-domain Score"));
        lookup.setWiderBarriersSubDomainRank(convertInt(record, "Wider Barriers Sub-domain Rank (where 1 is most deprived)"));
        lookup.setWiderBarriersSubDomainDecile(convertInt(record, "Wider Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setIndoorsSubDomainScore(convertDouble(record, "Indoors Sub-domain Score"));
        lookup.setIndoorsSubDomainRank(convertInt(record, "Indoors Sub-domain Rank (where 1 is most deprived)"));
        lookup.setIndoorsSubDomainDecile(convertInt(record, "Indoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setOutdoorsSubDomainScore(convertDouble(record, "Outdoors Sub-domain Score"));
        lookup.setOutdoorsSubDomainRank(convertInt(record, "Outdoors Sub-domain Rank (where 1 is most deprived)"));
        lookup.setOutdoorsSubDomainDecile(convertInt(record, "Outdoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)"));

        lookup.setTotalPopulation(convertInt(record, "Total population: mid 2012 (excluding prisoners)"));
        lookup.setDependentChildren0To15(convertInt(record, "Dependent Children aged 0-15: mid 2012 (excluding prisoners)"));
        lookup.setPopulation16To59(convertInt(record, "Population aged 16-59: mid 2012 (excluding prisoners)"));
        lookup.setOlderPopulation60AndOver(convertInt(record, "Older population aged 60 and over: mid 2012 (excluding prisoners)"));

        //this data seems to contain a non-integer value, which makes no sense, so ignoring it
        //lookup.setWorkingAgePopulation(convertInt(record, "Working age population 18-59/64: for use with Employment Deprivation Domain (excluding prisoners) ")); //note trailing space is in the source file

        ReferenceUpdaterDalI referenceUpdaterDal = DalProvider.factoryReferenceUpdaterDal();
        referenceUpdaterDal.updateDeprivationMap(lookup);

    }

    public static String[] getCsvHeadings() {
        return new String[]{
                "LSOA code (2011)",
                "LSOA name (2011)",
                "Local Authority District code (2013)",
                "Local Authority District name (2013)",
                "Index of Multiple Deprivation (IMD) Score",
                "Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)",
                "Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)",
                "Income Score (rate)",
                "Income Rank (where 1 is most deprived)",
                "Income Decile (where 1 is most deprived 10% of LSOAs)",
                "Employment Score (rate)",
                "Employment Rank (where 1 is most deprived)",
                "Employment Decile (where 1 is most deprived 10% of LSOAs)",
                "Education, Skills and Training Score",
                "Education, Skills and Training Rank (where 1 is most deprived)",
                "Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)",
                "Health Deprivation and Disability Score",
                "Health Deprivation and Disability Rank (where 1 is most deprived)",
                "Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)",
                "Crime Score",
                "Crime Rank (where 1 is most deprived)",
                "Crime Decile (where 1 is most deprived 10% of LSOAs)",
                "Barriers to Housing and Services Score",
                "Barriers to Housing and Services Rank (where 1 is most deprived)",
                "Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)",
                "Living Environment Score",
                "Living Environment Rank (where 1 is most deprived)",
                "Living Environment Decile (where 1 is most deprived 10% of LSOAs)",
                "Income Deprivation Affecting Children Index (IDACI) Score (rate)",
                "Income Deprivation Affecting Children Index (IDACI) Rank (where 1 is most deprived)",
                "Income Deprivation Affecting Children Index (IDACI) Decile (where 1 is most deprived 10% of LSOAs)",
                "Income Deprivation Affecting Older People (IDAOPI) Score (rate)",
                "Income Deprivation Affecting Older People (IDAOPI) Rank (where 1 is most deprived)",
                "Income Deprivation Affecting Older People (IDAOPI) Decile (where 1 is most deprived 10% of LSOAs)",
                "Children and Young People Sub-domain Score",
                "Children and Young People Sub-domain Rank (where 1 is most deprived)",
                "Children and Young People Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Adult Skills Sub-domain Score",
                "Adult Skills Sub-domain Rank (where 1 is most deprived)",
                "Adult Skills Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Geographical Barriers Sub-domain Score",
                "Geographical Barriers Sub-domain Rank (where 1 is most deprived)",
                "Geographical Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Wider Barriers Sub-domain Score",
                "Wider Barriers Sub-domain Rank (where 1 is most deprived)",
                "Wider Barriers Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Indoors Sub-domain Score",
                "Indoors Sub-domain Rank (where 1 is most deprived)",
                "Indoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Outdoors Sub-domain Score",
                "Outdoors Sub-domain Rank (where 1 is most deprived)",
                "Outdoors Sub-domain Decile (where 1 is most deprived 10% of LSOAs)",
                "Total population: mid 2012 (excluding prisoners)",
                "Dependent Children aged 0-15: mid 2012 (excluding prisoners)",
                "Population aged 16-59: mid 2012 (excluding prisoners)",
                "Older population aged 60 and over: mid 2012 (excluding prisoners)",
                "Working age population 18-59/64: for use with Employment Deprivation Domain (excluding prisoners) " //note trailing space is in the source file
        };
    }
}
