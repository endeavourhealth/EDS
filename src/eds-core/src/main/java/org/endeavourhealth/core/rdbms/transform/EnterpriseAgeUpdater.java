package org.endeavourhealth.core.rdbms.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.time.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EnterpriseAgeUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseAgeUpdater.class);

    public static final int UNIT_YEARS = 0;
    public static final int UNIT_MONTHS = 1;
    public static final int UNIT_WEEKS = 2;

    public static Integer[] calculateAgeValues(long patientId, Date dateOfBirth, String enterpriseConfigName) throws Exception {

        EntityManager entityManager = TransformConnection.getEntityManager();

        EnterpriseAge map = findAgeObject(patientId, entityManager);
        if (map == null) {
            map = new EnterpriseAge();
            map.setEnterprisePatientId(patientId);
        }

        //always re-set these, in case they've changed
        map.setDateOfBirth(dateOfBirth);
        map.setEnterpriseConfigName(enterpriseConfigName);

        Integer[] ret = calculateAgeValues(map);

        entityManager.getTransaction().begin();
        entityManager.persist(map);
        entityManager.getTransaction().commit();
        entityManager.close();
        return ret;
    }

    public static Integer[] calculateAgeValues(EnterpriseAge map) throws Exception {

        Integer[] ret = new Integer[3];

        if (map.getDateOfBirth() == null) {
            return ret;
        }

        LocalDate dobLocalDate = map.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate nowLocalDate = LocalDate.now();

        Period period = Period.between(dobLocalDate, nowLocalDate);
        ret[UNIT_YEARS] = new Integer(period.getYears());

        if (ret[UNIT_YEARS].intValue() < 5) {
            ret[UNIT_YEARS] = null;
            ret[UNIT_MONTHS] = new Integer((period.getYears() * 12) + period.getMonths());

            if (ret[UNIT_MONTHS].intValue() <= 12) {
                ret[UNIT_MONTHS] = null;

                //period doesn't help with calculating the number of days in each month, so use an alternative
                //method to calculate the number of days
                long millis = new Date().getTime() - map.getDateOfBirth().getTime();
                int days = (int)TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS);
                ret[UNIT_WEEKS] = new Integer(days / 7);
                //ret[UNIT_WEEKS] = new Integer(period.getDays() / 7);
            }
        }

        //store the dob for the updater job that updates Enterprise
        calculateNextUpdateDate(dobLocalDate, nowLocalDate, ret, map);

        return ret;
    }



    private static void calculateNextUpdateDate(LocalDate dobLocalDate,
                                                LocalDate nowLocalDate,
                                                Integer[] values,
                                                EnterpriseAge map) throws Exception {


        LocalDate nextUpdate = null;

        if (values[UNIT_YEARS] != null) {
            //if counting in years, we want to update next birthday
            int updateDay = dobLocalDate.getDayOfMonth();
            int updateMonth = dobLocalDate.getMonthValue();
            int updateYear = nowLocalDate.getYear();

            nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);

            //if we've already passed their birthday this year, then add a year so we update next year
            if (!nextUpdate.isAfter(nowLocalDate)) {
                updateYear ++;
                nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);
            }

        } else if (values[UNIT_MONTHS] != null) {
            //if counting in months, we want to update the day after the day of birth in the next month
            int updateDay = dobLocalDate.getDayOfMonth();
            int updateMonth = nowLocalDate.getMonthValue();
            int updateYear = nowLocalDate.getYear();

            nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);

            //if we've already passed the day in this month when we'd update, then roll forward to next month
            if (!nextUpdate.isAfter(nowLocalDate)) {
                if (updateMonth == Month.DECEMBER.getValue()) {
                    updateYear ++;
                } else {
                    updateMonth ++;
                }

                nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);
            }

        } else {
            int updateDay = dobLocalDate.getDayOfMonth();
            int updateMonth = dobLocalDate.getMonthValue();
            int updateYear = dobLocalDate.getYear();

            nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);

            //keep looping until we find a date after today, adding a week each time
            while (!nextUpdate.isAfter(nowLocalDate)) {
                updateDay += 7;

                //see if we've gone into the next month
                LocalDate dummy = LocalDate.of(updateYear, updateMonth, 1);
                int monthLen = dummy.lengthOfMonth();
                if (updateDay > monthLen) {
                    updateDay -= monthLen;
                    updateMonth ++;

                    if (updateMonth > 12) {
                        updateMonth = 1;
                        updateYear ++;
                    }
                }

                nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);
            }

            //if counting in weeks, we want to update the date after the day of birth in the next week
            /*int updateDay = dobLocalDate.getDayOfMonth() + 7;
            int updateMonth = nowLocalDate.getMonthValue();
            int updateYear = nowLocalDate.getYear();

            //check if we've got enough days remaining in this month for another check
            //next week, or see if we have to roll over into the first week of next month (or year)
            LocalDate dummy = LocalDate.of(updateYear, updateMonth, 1);
            int monthLen = dummy.lengthOfMonth();
            if (updateDay < monthLen) {
                nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);

            } else {
                updateDay -= monthLen;
                if (updateMonth == Month.DECEMBER.getValue()) {
                    updateYear ++;
                } else {
                    updateMonth ++;
                }

                nextUpdate = createSafeLocalDate(updateYear, updateMonth, updateDay);
            }*/
        }

        Date nextUpdateDate = Date.from(nextUpdate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        map.setDateNextChange(nextUpdateDate);


    }

    private static LocalDate createSafeLocalDate(int year, int month, int day) throws Exception {
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            //if we try to create a 29th Feb on a non-leap year, then we'll get this exception
            //so handle this and create as 1st March instead
            if (day == 29
                    && month == Month.FEBRUARY.getValue()) {
                return createSafeLocalDate(year, Month.MARCH.getValue(), 1);

            } else if (day == 31
                        && month == Month.APRIL.getValue()) {
                return createSafeLocalDate(year, Month.MAY.getValue(), 1);

            } else if (day == 31
                    && month == Month.JUNE.getValue()) {
                return createSafeLocalDate(year, Month.JULY.getValue(), 1);

            } else if (day == 31
                    && month == Month.SEPTEMBER.getValue()) {
                return createSafeLocalDate(year, Month.OCTOBER.getValue(), 1);

            } else if (day == 31
                    && month == Month.NOVEMBER.getValue()) {
                return createSafeLocalDate(year, Month.DECEMBER.getValue(), 1);

            } else {
                throw ex;
            }
        }
    }

    private static EnterpriseAge findAgeObject(long enterprisePatientId, EntityManager entityManager) throws Exception {

        String sql = "select c"
                + " from"
                + " EnterpriseAge c"
                + " where c.enterprisePatientId = :enterprisePatientId";


        Query query = entityManager.createQuery(sql, EnterpriseAge.class)
                .setParameter("enterprisePatientId", enterprisePatientId);

        try {
            return (EnterpriseAge)query.getSingleResult();

        } catch (NoResultException ex) {
            return null;
        }
    }


    /**
     * utility to update the Age columns in Enterprise
     *
     * Usage
     * =================================================================================
     * No parameters
     */
    /*public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("EnterpriseAgeUpdater");

        try {
            if (args.length != 0) {
                LOG.error("No parameters required.");
                return;
            }

            LOG.info("Age updater starting");

            EntityManager entityManager = TransformConnection.getEntityManager();
            List<EnterpriseAge> agesToUpdate = findAgesToUpdate(entityManager);
            LOG.info("Found " + agesToUpdate.size() + " ages to update");

            String currentConfigName = null;
            Connection currentConnection = null;

            int progress = 0;
            for (EnterpriseAge ageToUpdate: agesToUpdate) {

                Integer[] ages = calculateAgeValues(ageToUpdate);

                //see if we're on a different enterprise DB yet, loading the config if needed
                if (currentConfigName == null
                        || !currentConfigName.equalsIgnoreCase(ageToUpdate.getEnterpriseConfigName())) {

                    if (currentConnection != null) {
                        currentConnection.close();
                    }
                    currentConfigName = ageToUpdate.getEnterpriseConfigName();
                    JsonNode config = ConfigManager.getConfigurationAsJson(currentConfigName, "enterprise");
                    currentConnection = openConnection(config);
                }

                updateEnterprise(ageToUpdate.getEnterprisePatientId(), ages, currentConnection);

                //if we've successfully updated Enterprise, then it's time to save our updated map object
                //with the newly calculated date of next update
                entityManager.persist(ageToUpdate);

                progress ++;
                if (progress % 100 == 0) {
                    LOG.info("Done " + progress);
                }
            }

            if (currentConnection != null) {
                currentConnection.close();
            }

            entityManager.close();

            LOG.info("Age updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    private static Connection openConnection(JsonNode config) throws Exception {

        String driverClass = config.get("driverClass").asText();
        String url = config.get("url").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        //force the driver to be loaded
        Class.forName(driverClass);

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        return conn;
    }

    private static void updateEnterprise(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

        //the enterprise patient database isn't managed using hibernate, so we need to simply write a simple update statement
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE patient SET ");
        sb.append("age_years = ?, ");
        sb.append("age_months = ?, ");
        sb.append("age_weeks = ? ,");
        sb.append("WHERE id = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        if (ages[UNIT_YEARS] == null) {
            update.setNull(1, Types.INTEGER);
        } else {
            update.setInt(1, ages[UNIT_YEARS]);
        }

        if (ages[UNIT_MONTHS] == null) {
            update.setNull(2, Types.INTEGER);
        } else {
            update.setInt(2, ages[UNIT_MONTHS]);
        }

        if (ages[UNIT_WEEKS] == null) {
            update.setNull(3, Types.INTEGER);
        } else {
            update.setInt(3, ages[UNIT_WEEKS]);
        }

        update.addBatch();
        update.executeBatch();

        connection.commit();
    }

    private static List<EnterpriseAge> findAgesToUpdate(EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " EnterpriseAge c"
                + " where c.dateNextChange <= :dateNextChange"
                + " order by c.enterpriseConfigName";


        Query query = entityManager.createQuery(sql, EnterpriseAge.class)
                .setParameter("dateNextChange", new Date());

        return query.getResultList();
    }*/

}
