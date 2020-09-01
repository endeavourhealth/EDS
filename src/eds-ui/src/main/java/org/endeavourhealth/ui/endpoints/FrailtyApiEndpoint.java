package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Path("/frailtyApi")
public class FrailtyApiEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FrailtyApiEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="FrailtyApiEndpoint.recentStats")
    @Path("/recentStats")
    public Response getRecentStats(@Context SecurityContext sc,
                                   @QueryParam("minutesBack") int minutesBack,
                                   @QueryParam("groupBy") String groupBy) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Frailty API Recent Stats",
                "Minutes Back", minutesBack,
                "Group By", groupBy);

        //all the below should really be moved to core, but this is all one-off stuff (hopefully)
        List<FrailtyStat> ret = getRecentStats(minutesBack, groupBy);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name="FrailtyApiEndpoint.downloadMonthlyStats")
    @Path("/downloadMonthlyStats")
    public Response getRecentStats(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Download Frailty API Monthly Stats");

        String s = getMonthlyStats();

        clearLogbackMarkers();

        return Response
                .ok(s, MediaType.TEXT_PLAIN_TYPE)
                .build();
    }

    private static String getMonthlyStats() throws Exception {

        EntityManager entityManager = ConnectionManager.getAuditEntityManager();
        CallableStatement stmt = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String query = "{CALL get_monthly_frailty_stats()}";
            stmt = connection.prepareCall(query);

            ResultSet rs = stmt.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();
            String[] headers = new String[meta.getColumnCount()];
            for (int i=0; i<meta.getColumnCount(); i++) {
                headers[i] = meta.getColumnName(i+1);
            }

            CSVFormat format = CSVFormat.DEFAULT.withHeader(headers);

            StringWriter sw = new StringWriter();
            CSVPrinter csv = new CSVPrinter(sw, format);

            while (rs.next()) {

                List<Object> vals = new ArrayList<>();
                for (int i=0; i<meta.getColumnCount(); i++) {
                    Object o = rs.getObject(i+1);
                    vals.add(o);
                }
                csv.printRecord(vals);
            }

            csv.flush();
            csv.close();

            return sw.toString();

        } finally {
            if (stmt != null) {
                stmt.close();
            }
            entityManager.close();
        }
    }

    private static List<FrailtyStat> getRecentStats(int minutesBack, String groupBy) throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MINUTE, -minutesBack);
        Date earliestDate = cal.getTime();

        cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.MILLISECOND, 0);

        int groupMs;

        //roll forwards to the next suitable end, based on the grouping
        if (groupBy.equalsIgnoreCase("second")) {
            //move to end of current second
            cal.add(Calendar.SECOND, 1);

            groupMs = 1000;

        } else if (groupBy.equalsIgnoreCase("minute")) {
            //move to end of current minute
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.MINUTE, 1);

            groupMs = 1000 * 60;

        } else if (groupBy.equalsIgnoreCase("5minute")) {
            //roll forwards to end of current 5 min period
            cal.set(Calendar.SECOND, 0);
            rollForwards(cal, Calendar.MINUTE, 5);

            groupMs = 1000 * 60 * 5;

        } else if (groupBy.equalsIgnoreCase("10minute")) {
            //roll forwards to end of current 10 min period
            cal.set(Calendar.SECOND, 0);
            rollForwards(cal, Calendar.MINUTE, 10);

            groupMs = 1000 * 60 * 10;

        } else if (groupBy.equalsIgnoreCase("30minute")) {
            //roll forwards to end of current 30 min period
            cal.set(Calendar.SECOND, 0);
            rollForwards(cal, Calendar.MINUTE, 30);

            groupMs = 1000 * 60 * 30;

        } else if (groupBy.equalsIgnoreCase("hour")) {
            //move to end of the current hour
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.add(Calendar.HOUR_OF_DAY, 1);

            groupMs = 1000 * 60 * 60;

        } else {
            throw new Exception("Unexpected grouping " + groupBy);
        }

        Date latestDate = cal.getTime();

        List<FrailtyStat> stats = new ArrayList<>();

        //create a statistic record for each minute
        while (latestDate.after(earliestDate)) {
            cal = Calendar.getInstance();
            cal.setTime(latestDate);
            cal.add(Calendar.MILLISECOND, -groupMs);
            Date dStart = cal.getTime();
            if (dStart.before(earliestDate)) {
                earliestDate = dStart;
            }
            Date dEnd = latestDate;

            stats.add(new FrailtyStat(dStart, dEnd));

            latestDate = dStart;
        }

        EntityManager entityManager = ConnectionManager.getAuditEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = "SELECT timestmp, response_code, response_body" +
                    " FROM subscriber_api_audit" +
                    " WHERE timestmp >= ?";

            ps = connection.prepareStatement(sql);

            ps.setTimestamp(1, new Timestamp(earliestDate.getTime()));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int col = 1;

                Date d = new Date(rs.getTimestamp(col++).getTime());
                int responseCode = rs.getInt(col++);
                String responseBody = rs.getString(col++);

                FrailtyStat stat = null;
                for (FrailtyStat s: stats) {
                    if (!d.before(s.getdFrom())
                            && d.before(s.getdTo())) {
                        stat = s;
                        break;
                    }
                }
                if (stat == null) {
                    throw new Exception("Failed to find stat for date " + d);
                }

                if (responseCode == 200) {
                    if (responseBody.contains("Potentially")) {
                        stat.incrementMatchedAndFrail();
                    } else {
                        stat.incremantMatchedAndNotFrail();
                    }

                } else {
                    if (responseBody != null && responseBody.contains("No patient record could be found")) {
                        stat.incrementErrorNotMatched();
                    } else {
                        stat.incrementErrorOther();
                    }
                }

                stat.incrementTotal();
            }

            return stats;

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }

    }

    private static void rollForwards(Calendar cal, int field, int amount) {
        while (true) {
            int min = cal.get(field);
            if (min % amount == 0) {
                break;
            }
            cal.add(field, 1);
        }
    }

    static class FrailtyStat {
        Date dFrom;
        Date dTo;
        int matchedAndFrail = 0;
        int matchedAndNotFrail = 0;
        int errorNotMatched = 0;
        int errorOther = 0;
        int total = 0;

        public FrailtyStat(Date dFrom, Date dTo) {
            this.dFrom = dFrom;
            this.dTo = dTo;
        }

        public Date getdFrom() {
            return dFrom;
        }

        public Date getdTo() {
            return dTo;
        }

        public int getMatchedAndFrail() {
            return matchedAndFrail;
        }

        public void incrementMatchedAndFrail() {
            this.matchedAndFrail ++;
        }

        public int getMatchedAndNotFrail() {
            return matchedAndNotFrail;
        }

        public void incremantMatchedAndNotFrail() {
            this.matchedAndNotFrail ++;
        }

        public int getErrorNotMatched() {
            return errorNotMatched;
        }

        public void incrementErrorNotMatched() {
            this.errorNotMatched ++;
        }

        public int getErrorOther() {
            return errorOther;
        }

        public void incrementErrorOther() {
            this.errorOther ++;
        }

        public int getTotal() {
            return total;
        }

        public void incrementTotal() {
            this.total ++;
        }
    }
}

